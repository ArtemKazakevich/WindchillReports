package ext.by.iba.OneC;

import wt.iba.definition.litedefinition.*;
import wt.iba.definition.service.IBADefinitionHelper;
import wt.iba.definition.IBADefinitionException;
import wt.iba.definition.DefinitionLoader;
import wt.iba.value.litevalue.*;
import wt.iba.value.IBAValueException;
import wt.iba.value.IBAReferenceable;
import wt.iba.value.IBAHolder;
import wt.iba.value.DefaultAttributeContainer;
import wt.iba.value.service.IBAValueHelper;
import wt.iba.constraint.IBAConstraintException;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.util.WTInvalidParameterException;
import wt.access.NotAuthorizedException;
import wt.part.*;
import wt.fc.*;
import wt.inf.container.WTContainerHelper;
import wt.inf.container.WTContainerRef;
import wt.folder.FolderHelper;
import wt.folder.Folder;
import wt.folder.FolderEntry;
import wt.introspection.ReflectionHelper;
import wt.vc.views.ViewHelper;
import wt.vc.views.View;
import wt.vc.views.ViewException;
import wt.vc.views.ViewManageable;
import wt.vc.wip.Workable;
import wt.vc.wip.ObjectsAlreadyCheckedOutException;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.*;
import wt.vc.config.LatestConfigSpec;
import wt.load.LocatorFactory;
import wt.query.QuerySpec;
import wt.query.QueryException;
import wt.query.SearchCondition;
import wt.pds.StatementSpec;
import wt.clients.vc.CheckInOutTaskLogic;
import wt.lifecycle.LifeCycleException;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.LifeCycleTemplate;
import wt.lifecycle.State;
import wt.lifecycle.StandardLifeCycleService;
import java.util.*;
import java.rmi.RemoteException;
import java.io.IOException;

import com.ptc.core.meta.common.TypeIdentifier;
import com.infoengine.jsp.InfoEngine;
import com.infoengine.SAK.ObjectWebject;
import com.infoengine.util.IEException;

//import ext.by.iba.WfHelpers.GetterAttrsValue;

/**
 * Created by IntelliJ IDEA. User: Shydlouski_V Date: 14.01.2011 Time: 16:14:32
 * Служебный класс многие методы которого слизаны мной с PTC-шного
 * iba.value.service.LoadValue. Имена этих "слизанных" методов сохранены.
 */
public class IBAJVLoader {
	private boolean VERBOSE;
	private Hashtable definition_cache;
	private String containerRefPKI;
	private String containerRefMAT;
	private String folderPKI;
	private String folderMAT;
	private String nameView;
	private String stateDelete;

	public IBAJVLoader(String containerRefPKI, String containerRefMAT,
			String folderPKI, String folderMAT, String nameView,
			String stateDelete, boolean verbose) {
		this.containerRefPKI = containerRefPKI;
		this.containerRefMAT = containerRefMAT;
		this.folderPKI = folderPKI;
		this.folderMAT = folderMAT;
		this.nameView = nameView;
		this.stateDelete = stateDelete;
		this.VERBOSE = verbose;
		this.definition_cache = new Hashtable();
	}

	public String addObjectInWindchill(Map<String, String> object) {
		List<String> errorSoftAttrs;
		String result = "OK";
		WTPart existPart, newPart = null;
		String name = (String) object.get("name");
		System.out.println((new StringBuilder()).append("----- new Object ")
				.append(name).append(" -------------").toString());

		try {
			existPart = getPart((String) object.get("number"), null, null, null); // findChangedPart(
			// (String)object.get("number"));
			if (existPart != null) {
				if (existPart.getLifeCycleState().equals(
						State.toState(this.stateDelete))) {
					result = changeStateFromDelete(existPart);
					if (VERBOSE)
						System.out
								.println("!!! adding (return from Delete) -> to 1C !!!"
										+ result);
					return result;
				} else {
					if (VERBOSE)
						System.out
								.println("!!! adding -> to 1C !!! - There is an object with this NUMBER in Windchill");
					// / ??? createNewViewVersion( (String)object.get("number"),
					// null, null, "Design", "Manufacturing");
					return "Объект уже есть в Windchill.";
				}
			}

			newPart = constructPart(object, newPart);
			if (newPart == null) {
				System.out.println("!!! adding -> to 1C !!! - " + result);
				return "Не смог сконструировать объект " + name;
			}
			object.remove("Action");
			object.remove("Type");
			object.remove("unit");
			object.remove("name");
			object.remove("number"); // должны остаться одни Soft атрибуты
			errorSoftAttrs = addSoftAttrs(object, newPart);
			if (errorSoftAttrs.isEmpty())
				PersistenceHelper.manager.save(newPart);
			else
				result = (new StringBuilder()).append(
						"Проблема при создании с name=").append(name).append(
						" - ").append(errorSoftAttrs.toString()).toString();
		} catch (WTPropertyVetoException e) {
			e.printStackTrace();
			result = (new StringBuilder()).append(
					"Проблема при создании с именем или значением в name=")
					.append(name).append(" - ").append(
							e.getMessage().replaceAll(":", " ")).toString();
		} catch (WTInvalidParameterException e) {
			e.printStackTrace();
			result = (new StringBuilder())
					.append(
							"Недопустимое значение атрибута (б.всего unit) при создании name=")
					.append(name).toString();
		} catch (RemoteException e) {
			e.printStackTrace();
			result = (new StringBuilder()).append(
					"RemoteException при создании name=").append(name).append(
					" - ").append(e.getMessage().replaceAll(":", " "))
					.toString();
		} catch (WTException e) {
			e.printStackTrace();
			result = (new StringBuilder()).append(
					"Проблема при создании с name=").append(name).append(" - ")
					.append(e.getMessage().replaceAll(":", " ")).toString();
		} catch (Exception e) {
			e.printStackTrace();
			result = (new StringBuilder()).append(
					"Exception при создании name=").append(name).append(" - ")
					.append(e.getMessage().replaceAll(":", " ")).toString();
		}

		System.out.println("!!! adding -> to 1C !!! - " + result);
		return result;
	}

	/*
	 * !!!! метод который нужно переписать или добавить свой для поиска по id //
	 * этот метож вызывался до правок Bykovskaya
	 */
	public String changeObjectInWindchill(Map<String, String> object) {
		String result = "OK";
		List<String> errorSoftAttrs;
		WTPart existPart;
		String name = (String) object.get("name");
		String number = (String) object.get("number");
		Workable workable;
		System.out.println((new StringBuilder()).append("----- change Object ")
				.append(number).append(" -------------").toString());

		try {
			/* !!!! поиск парта нужно переписать, чтобы искала по Object ID */

			existPart = getPart(number, null, null, null); // findChangedPart(
			// number);

			if (existPart == null) {
				result = (new StringBuilder()).append("Объект ").append(number)
						.append(" ").append("не найден для редактироывания !")
						.toString();
				if (VERBOSE)
					System.out.println("!!! changing -> to 1C !!! " + result);
				return result;
			}

			if (existPart.getLifeCycleState().equals(
					State.toState(this.stateDelete)))
				result = changeStateFromDelete(existPart);

			if (!result.equals("OK"))
				return result;
			// // переписать переименование еще и обозначения
			if (!object.get("name").equals(existPart.getName()))
				changeIdentityPart(existPart, name);

			object.remove("Action");
			object.remove("Type");
			object.remove("unit");
			object.remove("name");
			object.remove("number"); // должны остаться одни Soft атрибуты

			CheckInOutTaskLogic.checkOutObject(existPart, CheckInOutTaskLogic
					.getCheckoutFolder(), "Изменение soft атрибутов");
			workable = CheckInOutTaskLogic.getWorkingCopy(existPart);

			errorSoftAttrs = editSoftAttrs(object, workable);
			if (errorSoftAttrs.isEmpty()) {
				PersistenceHelper.manager.save(workable);
				CheckInOutTaskLogic.checkInObject(workable,
						"Атрибуты изменены по запросу 1C.");
			} else {
				result = (new StringBuilder()).append(
						"Проблема при изменении с ").append(name).append("  ")
						.append(number).append(" - ").append(
								errorSoftAttrs.toString()).toString();
				CheckInOutTaskLogic.undoCheckout(workable);
			}

		} catch (QueryException e) {
			e.printStackTrace();
			result = (new StringBuilder()).append(
					"QueryException при изменении number=").append(number)
					.append(" - ").append(e.getMessage().replaceAll(":", " "))
					.toString();
		} catch (ObjectsAlreadyCheckedOutException e) {
			e.printStackTrace();
			result = (new StringBuilder()).append(
					"AlreadyCheckedOutException при изменении number=").append(
					number).toString();
		} catch (WTPropertyVetoException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
			result = (new StringBuilder()).append(
					"RemoteException при изменении number=").append(number)
					.append(" - ").append(e.getMessage().replaceAll(":", " "))
					.toString();
		} catch (WTException e) {
			e.printStackTrace();
			result = (new StringBuilder()).append(
					"WTException при изменении number=").append(number).append(
					" - ").append(e.getMessage().replaceAll(":", " "))
					.toString();
		} catch (Exception e) {
			e.printStackTrace();
			result = (new StringBuilder()).append(
					"Exception при изменении number=").append(number).append(
					" - ").append(e.getMessage().replaceAll(":", " "))
					.toString();
		}
		System.out.println("!!! changing -> to 1C !!! - " + result);
		return result;
	}

	/**
	 * edit wtPart
	 * 
	 * @author Bykovskaya
	 *            map str from 1C
	 * @return str result
	 */
	public String changeObjectInWindchillID(Map<String, String> object) {

		String result = "OK";
		List<String> errorSoftAttrs;
		WTPart existPart;
		String name = (String) object.get("name");
		String number = (String) object.get("number");
		String oidMaster = (String) object.get("ObjectID");
		Workable workable;
		System.out.println((new StringBuilder()).append("----- change Object ")
				.append(number).append(" -------------").toString());
		if ((oidMaster != null) && !(oidMaster.isEmpty())) {
			try {
				/* !!!! поиск парта по Object ID */
				existPart = findChangedPartFromID(oidMaster);
				if (existPart == null) {
					result = (new StringBuilder()).append("Объект ").append(
							number).append(" ").append("oid ")
							.append(oidMaster).append(" ").append(
									"не найден для редактироывания !")
							.toString();
					if (VERBOSE)
						System.out.println("!!! changing -> to 1C !!! "
								+ result);
					return result;
				} else {
					System.out
							.println("!!! changing windchill part -> to 1C !!! "
									+ existPart.getNumber()
									+ " ; "
									+ existPart.getNumber());

				}
				System.out.println("!!! result is  " + result);
				if (existPart.getLifeCycleState().equals(
						State.toState(this.stateDelete)))
					result = changeStateFromDelete(existPart);

				if (!result.equals("OK"))
					return result;
				// // переписать переименование еще и обозначения
				// решили не брать наименование из 1С
				/*if (!object.get("name").equals(existPart.getName())) {
					changeIdentityPart(existPart, name);
					System.out
							.println("!!! rename name windchill part -> to 1C !!! "
									+ existPart.getNumber()
									+ " ; "
									+ existPart.getNumber());
				}*/
				if (!object.get("number").equals(existPart.getNumber())) {
					changeIdentityPartNumber(existPart, number);
					System.out
							.println("!!! rename number windchill part -> to 1C !!! "
									+ existPart.getNumber()
									+ " ; "
									+ existPart.getNumber());
				}
				// должны остаться одни Soft атрибуты
				object.remove("Action");
				object.remove("Type");
				object.remove("unit");
				object.remove("name");
				object.remove("number");
				object.remove("ObjectID");

				CheckInOutTaskLogic.checkOutObject(existPart,
						CheckInOutTaskLogic.getCheckoutFolder(),
						"Изменение soft атрибутов");
				System.out
						.println("!!! checkOutObject windchill part ->!!! "
								+ existPart.getNumber() + " ; "
								+ existPart.getNumber());
				workable = CheckInOutTaskLogic.getWorkingCopy(existPart);

				errorSoftAttrs = editSoftAttrs(object, workable);

				System.out.println("!!! errorSoftAttrs  !!! " + errorSoftAttrs);
				if (errorSoftAttrs.isEmpty()) {
					PersistenceHelper.manager.save(workable);
					CheckInOutTaskLogic.checkInObject(workable,
							"Атрибуты изменены по запросу 1C.");
				} else {
					result = (new StringBuilder()).append(
							"Проблема при изменении с ").append(name).append(
							"  ").append(number).append(" - ").append(
							errorSoftAttrs.toString()).toString();
					CheckInOutTaskLogic.undoCheckout(workable);
				}

			} catch (QueryException e) {
				e.printStackTrace();
				result = (new StringBuilder()).append(
						"QueryException при изменении number=").append(number)
						.append(" - ").append(
								e.getMessage().replaceAll(":", " ")).toString();
			} catch (ObjectsAlreadyCheckedOutException e) {
				e.printStackTrace();
				result = (new StringBuilder()).append(
						"AlreadyCheckedOutException при изменении number=")
						.append(number).toString();
			} catch (WTPropertyVetoException e) {
				e.printStackTrace();
			} catch (RemoteException e) {
				e.printStackTrace();
				result = (new StringBuilder()).append(
						"RemoteException при изменении number=").append(number)
						.append(" - ").append(
								e.getMessage().replaceAll(":", " ")).toString();
			} catch (WTException e) {
				e.printStackTrace();
				result = (new StringBuilder()).append(
						"WTException при изменении number=").append(number)
						.append(" - ").append(
								e.getMessage().replaceAll(":", " ")).toString();
			} catch (Exception e) {
				e.printStackTrace();
				result = (new StringBuilder()).append(
						"Exception при изменении number=").append(number)
						.append(" - ").append(
								e.getMessage().replaceAll(":", " ")).toString();
			}
		} else {
			result = (new StringBuilder()).append(
					"Exception oid is null, PKI number=").append(number)
					.toString();
		}
		System.out.println("!!! changing -> to 1C !!! - " + result);
		return result;
	}

	/**
	 * Реально объект не удаляем, а переводим его в заданное состояние его ЖЦ.
	 * 
	 * @param object
	 * @return result
	 */
	public String deleteObjectInWindchill(Map<String, String> object) {
		String result = "OK";
		WTPart existPart = null;
		String number = (String) object.get("number");
		System.out.println((new StringBuilder()).append("----- delete Object ")
				.append(number).append(" -------------").toString());

		try {
			existPart = findChangedPart(number);
			if (existPart == null) {
				result = (new StringBuilder()).append("Объект ").append(number)
						.append(" ").append("не найден для удаления !")
						.toString();
				System.out.println("!!! deleting to 1C !!! - " + result);
				return result;
			}
			if (VERBOSE)
				System.out.println("--> Changing state");

			StandardLifeCycleService lcService = StandardLifeCycleService
					.newStandardLifeCycleService();
			if (!lcService.isState((LifeCycleTemplate) existPart
					.getLifeCycleTemplate().getObject(), State
					.toState(stateDelete))) {
				result = (new StringBuilder()).append(
						"В ЖЦ объекта отсутствует состояние ").append(
						stateDelete).toString();
				return result;
			}

			// / почему то не работает, ломается у них внутрях
			// lcService.setLifeCycleState( existPart,
			// State.toState(stateDelete), true); // true - это прервать
			// связанные процессы
			if (!WorkInProgressHelper.isCheckedOut(existPart))
				// if(
				// existPart.getCheckoutInfo().getState().getStringValue().equals("wt.vc.wip.WorkInProgressState.c/i"))
				LifeCycleHelper.service.setLifeCycleState(
						(LifeCycleManaged) existPart, State
								.toState(stateDelete));
			else
				result = (new StringBuilder()).append(
						"Ошибка при \"удалении\" ").append(
						existPart.getNumber()).append(
						" т.к. объект взят на изменение.").toString();
		} catch (WTInvalidParameterException e) {
			result = (new StringBuilder()).append(
					"WTInvalidParameterException при \"удалении\"").toString();
			e.printStackTrace();
		} catch (LifeCycleException e) {
			result = (new StringBuilder()).append(
					"LifeCycleException при \"удалении\" ").append(
					existPart.getNumber()).toString();
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		}
		if (VERBOSE)
			System.out.println("--> State was changed to " + stateDelete);

		System.out.println("!!! deleting to 1C !!! - " + result);
		return result;
	}

	/**
	 * Реально объект не удаляем, а переводим его в заданное состояние его ЖЦ.
	 * 
	 * @author Bykovskaya
	 *            map str from 1C
	 * @return str result
	 */
	public String deleteObjectInWindchillID(Map<String, String> object) {
		String result = "OK";
		WTPart existPart = null;
		String number = (String) object.get("number");
		String oidMaster = (String) object.get("ObjectID");
		System.out.println((new StringBuilder()).append("----- delete Object ")
				.append(number).append(" -------------").toString());

		try {
			existPart = findChangedPartFromID(oidMaster);
			if (existPart == null) {
				result = (new StringBuilder()).append("Объект ").append(number)
						.append(" ").append("не найден для удаления !")
						.toString();
				System.out.println("!!! deleting to 1C !!! - " + result);
				return result;
			}
			if (VERBOSE)
				System.out.println("--> Changing state");

			StandardLifeCycleService lcService = StandardLifeCycleService
					.newStandardLifeCycleService();
			if (!lcService.isState((LifeCycleTemplate) existPart
					.getLifeCycleTemplate().getObject(), State
					.toState(stateDelete))) {
				result = (new StringBuilder()).append(
						"В ЖЦ объекта отсутствует состояние ").append(
						stateDelete).toString();
				return result;
			}

			// / почему то не работает, ломается у них внутрях
			// lcService.setLifeCycleState( existPart,
			// State.toState(stateDelete), true); // true - это прервать
			// связанные процессы
			if (!WorkInProgressHelper.isCheckedOut(existPart))
				// if(
				// existPart.getCheckoutInfo().getState().getStringValue().equals("wt.vc.wip.WorkInProgressState.c/i"))
				LifeCycleHelper.service.setLifeCycleState(
						(LifeCycleManaged) existPart, State
								.toState(stateDelete));
			else
				result = (new StringBuilder()).append(
						"Ошибка при \"удалении\" ").append(
						existPart.getNumber()).append(
						" т.к. объект взят на изменение.").toString();
		} catch (WTInvalidParameterException e) {
			result = (new StringBuilder()).append(
					"WTInvalidParameterException при \"удалении\"").toString();
			e.printStackTrace();
		} catch (LifeCycleException e) {
			result = (new StringBuilder()).append(
					"LifeCycleException при \"удалении\" ").append(
					existPart.getNumber()).toString();
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		}
		if (VERBOSE)
			System.out.println("--> State was changed to " + stateDelete);

		System.out.println("!!! deleting to 1C !!! - " + result);
		return result;
	}

	private WTPart constructPart(Map<String, String> object, WTPart newPart)
			throws WTInvalidParameterException, Exception {
		String containerObj, folderObj, typeObj;

		try {
			if (((String) object.get("Type")).equalsIgnoreCase("материал")) { // Проверить
				// букву
				// -
				// л
				// из
				// xml
				// !!!
				containerObj = containerRefMAT;
				folderObj = folderMAT;
				typeObj = "WCTYPE|wt.part.WTPart"; // "WCTYPE|wt.part.WTPart|by.peleng.material"
				// in 9.1
			} else {
				containerObj = containerRefPKI;
				folderObj = folderPKI;
				typeObj = "WCTYPE|wt.part.WTPart"; // "WCTYPE|wt.part.WTPart|by.peleng.supply"
				// in 9.1
			}

			WTContainerRef wtcontainerref = WTContainerHelper.service
					.getByPath(containerObj);
			Folder folder = FolderHelper.service.getFolder(folderObj,
					wtcontainerref);
			TypeIdentifier typeidentifier = (TypeIdentifier) ReflectionHelper
					.dynamicInvoke(
							"com.ptc.core.foundation.type.server.impl.TypeHelper",
							"getTypeIdentifier", new Class[] { String.class },
							new Object[] { typeObj });

			Object o = ReflectionHelper.dynamicInvoke(
					"com.ptc.core.foundation.type.server.impl.TypeHelper",
					"newInstance", new Class[] { TypeIdentifier.class },
					new Object[] { typeidentifier });

			if (!(o instanceof WTPart)) {
				throw new WTException(
						"!!!!!!!! Expected instance of wtpart, but was: "
								+ o.getClass().getName());
			}
			newPart = (WTPart) o;

			// Construct a default IterationInfo object. Cannot do the same for
			// the VersionInfo
			// object because there isn't enough information in the object to
			// look up the correct
			// version series from the OIRs yet.
			if (newPart.getIterationInfo() == null)
				newPart.setIterationInfo(IterationInfo.newIterationInfo());

			newPart.setName((String) object.get("name"));
			newPart.setNumber((String) object.get("number"));
			newPart.setContainerReference(wtcontainerref);
			newPart.setDefaultUnit(QuantityUnit.toQuantityUnit((String) object
					.get("unit")));
			FolderHelper.assignLocation((FolderEntry) newPart, folder); // до
			// сохранения
			// в БД
			// !!!
			newPart.setPartType(PartType.toPartType("component")); // это
			// <partType>
			// в XML или
			// "Assembly Mode"
			// ("режим сборки")
			// в UI
			newPart.setSource(Source.BUY);
			ViewHelper.assignToView(newPart, ViewHelper.service
					.getView(nameView)); // на plmsrv - "Design", на Пеленге10.1
			// "К"

		} catch (Exception e) {
			e.printStackTrace();
			newPart = null; // хоть он и так null. Это если чего с правами и
			// контейнером не так.
		}
		return newPart;
	}

	private List<String> addSoftAttrs(Map<String, String> object, WTPart newPart)
			throws WTException, RemoteException {
		List<String> errorSoftAttrs = new ArrayList<String>();
		AbstractAttributeDefinizerView abstractattributedefinizerview;
		AbstractValueView abstractvalueview;
		IBAHolder ibaholder = IBAValueHelper.service
				.refreshAttributeContainerWithoutConstraints(newPart);
		DefaultAttributeContainer defaultattrcontainer = (DefaultAttributeContainer) ibaholder
				.getAttributeContainer();

		Map.Entry<String, String> entry;
		Iterator<Map.Entry<String, String>> itm = object.entrySet().iterator();
		while (itm.hasNext()) {
			entry = itm.next();
			// System.out.println( entry.getKey().toString() +"="+
			// entry.getValue().toString() );
			abstractattributedefinizerview = getCachedAttributeDefinition(entry
					.getKey().toString()); // получаем из Windchilla определение
			// атрибута
			if (abstractattributedefinizerview == null) {
				errorSoftAttrs.add("Атрибут не найден в Windchill - "
						+ entry.getKey().toString());
				continue;
			} else {
				abstractvalueview = internalCreateValue(
						abstractattributedefinizerview, entry.getValue()
								.toString(),
						"wt.csm.navigation.ClassificationNode");
				if (abstractvalueview == null) {
					errorSoftAttrs.add("Не смог создать Value для - "
							+ entry.getKey().toString());
					continue;
				}
			}
			if (errorSoftAttrs.isEmpty())
				defaultattrcontainer.addAttributeValue(abstractvalueview);
		}
		return errorSoftAttrs;
	}

	private List<String> editSoftAttrs(Map<String, String> object,
			Workable workable) throws WTException, RemoteException {
		List<String> errorSoftAttrs = new ArrayList<String>();
		String oldValue;
		String result;

		AbstractAttributeDefinizerView abstractattributedefinizerview;
		AbstractValueView abstractvalueview;
		IBAHolder ibaholder = IBAValueHelper.service
				.refreshAttributeContainerWithoutConstraints((IBAHolder) workable);
		DefaultAttributeContainer defaultattrcontainer = (DefaultAttributeContainer) ibaholder
				.getAttributeContainer();
		Map.Entry<String, String> entry;
		Iterator<Map.Entry<String, String>> itm = object.entrySet().iterator();
		while (itm.hasNext()) {
			entry = itm.next();
			// System.out.println( entry.getKey().toString() +"="+
			// entry.getValue().toString() );
			abstractattributedefinizerview = getCachedAttributeDefinition(entry
					.getKey().toString()); // получаем из Windchilla определение
			// атрибута
			if (abstractattributedefinizerview == null) {
				errorSoftAttrs
						.add("Изменяемый в 1С атрибут не найден в Windchill - "
								+ entry.getKey().toString());
			} else {
				AbstractValueView aabstractvalueview[] = defaultattrcontainer
						.getAttributeValues((AttributeDefDefaultView) abstractattributedefinizerview);
				if (aabstractvalueview.length < 1) {
					if (VERBOSE)
						System.out.println("Attribute didn't exist on part");
					abstractvalueview = internalCreateValue(
							abstractattributedefinizerview, entry.getValue()
									.toString(),
							"wt.csm.navigation.ClassificationNode");
					if (abstractvalueview == null) {
						errorSoftAttrs.add("Не смог создать Value для - "
								+ entry.getKey().toString());
						continue;
					}
					defaultattrcontainer.addAttributeValue(abstractvalueview);
				} else {
					if (VERBOSE)
						System.out
								.println("Attribute already exist on part ... updating");
					oldValue = getOldValueAttr(aabstractvalueview[0]);
					if (oldValue != entry.getValue()) { // ( !oldValue.equals(
						// entry.getValue()))
						if (VERBOSE)
							System.out
									.println("Set a new value for this attribute.");
						result = setNewValue(aabstractvalueview[0], entry
								.getValue().toString(), defaultattrcontainer,
								abstractattributedefinizerview);
						if (result != null)
							errorSoftAttrs.add(result);
					} else if (VERBOSE)
						System.out.println("Attribute's value didn't change.");
				}
			}
		} // while

		return errorSoftAttrs;
	}

	private WTPart findChangedPart(String number) throws QueryException,
			WTException {
		WTPart part = null;
		QuerySpec mySpec = new QuerySpec(WTPart.class);
		SearchCondition searchCond = new SearchCondition(WTPart.class,
				WTPart.NUMBER, SearchCondition.EQUAL, number);
		// SearchCondition searchCond2 = new SearchCondition(
		// wt.part.WTPart.class, wt.part.WTPart.LATEST_ITERATION,
		// SearchCondition.EQUAL, new Boolean("true"));
		// mySpec.appendWhere( searchCond, new int[] {0});
		// mySpec.appendAnd();
		mySpec.appendWhere(searchCond, new int[] { 0 });
		QueryResult qr = PersistenceHelper.manager.find((StatementSpec) mySpec);

		if (VERBOSE) {
			// System.out.println("!!!!! " + mySpec.toString()); выводит SQL
			// запрос
			System.out.println("!!!! QueryResult's size - " + qr.size());
		}

		while (qr.hasMoreElements()) {
			Object p = qr.nextElement();
			if (p instanceof WTPart) {
				if (VERBOSE)
					System.out.println("Found Part - " + ((WTPart) p).getName()
							+ "   " + ((WTPart) p).getNumber() + "  "
							+ ((WTPart) p).isLatestIteration());
				if (((WTPart) p).isLatestIteration())
					part = (WTPart) p;
			} else {
				part = null; // на всякий случай.
				break;
			}
		}
		return part;
	}

	private void changeIdentityPart(WTPart existPart, String newName)
			throws IEException, IOException {
		/*
		 * WTPartMaster newPartMaster = new WTPartMaster();
		 * newPartMaster.setName( candidateName); newPartMaster.setNumber(
		 * candidateNumber); IdentityHelper.service.changeIdentity(
		 * (Identified)existPart.getMaster(),
		 * WTPartMasterIdentity.newWTPartMasterIdentity( newPartMaster));
		 */
		if (VERBOSE)
			System.out.println("--> Changing Identity");
		InfoEngine ie = new InfoEngine();
		String instance = wt.util.WTProperties.getLocalProperties()
				.getProperty("wt.federation.ie.VMName");

		ObjectWebject ow = new ObjectWebject("Change-Identity");
		ow.setService(ie);
		ow.addParam("INSTANCE", instance);
		// ow.addParam("DBUSER", DbUser); в сессии уже установлен Админ (см.
		// ChangeIdentity() )
		// ow.addParam("PASSWD", Passw);

		ow.addParam("WHERE", "number=" + existPart.getNumber());
		ow.addParam("FIELD", "name=" + newName);
		ow.addParam("TYPE", "wt.part.WTPart");
		ow.addParam("GROUP_OUT", "editIdentity");
		ow.invoke();

	}

	/**
	 * rename wtpart
	 * 
	 * @author Bykovskaya
	 * @param existPart
	 * @param newNumber
	 */
	private void changeIdentityPartNumber(WTPart existPart, String newNumber) {
		boolean uniqNumber = checkNumberPart(newNumber);
		WTPartMaster masterPart = (WTPartMaster) existPart.getMaster();
		if (uniqNumber) {
			try {
				// change WTPart identiry i.e. rename
				WTPartMasterIdentity partIdentity = (WTPartMasterIdentity) masterPart
						.getIdentificationObject();
				partIdentity.setNumber(newNumber);
				IdentityHelper.service.changeIdentity(masterPart, partIdentity);
				PersistenceHelper.manager.refresh(masterPart);
				if (VERBOSE)
					System.out.println(" RENAME WTPart DONE - " + newNumber);

			}// try
			catch (WTException ex) {
				System.out.println(" ERROR rename is false - " + newNumber);
				// ex.printStackTrace();
			} catch (WTPropertyVetoException e) {
				// TODO Auto-generated catch block
				System.out.println("  ERROR rename is false - " + newNumber);
				// e.printStackTrace();
			}
		} else {
			System.out.println(" NEW NUMBER IS NOT UNIQUE - " + newNumber);
		}

	}

	/**
	 * @author Bykovskaya check number wtpart in windchill
	 * @param number
	 * @return boollean result
	 */
	private boolean checkNumberPart(String number) {
		List<WTPartMaster> mparts = getListWTPartMasters(number);
		return ((mparts == null) || (mparts.isEmpty()));
	}

	/**
	 * get list of WTPartMaster from number
	 * 
	 * @param number wtpart
	 * @return list of WTPartMaster
	 */
	@Deprecated
	private List<WTPartMaster> getListWTPartMasters(String number) {
		List<WTPartMaster> mparts = new ArrayList<WTPartMaster>();
		QuerySpec qs = null;
		if (VERBOSE)
			System.out.println((new StringBuilder()).append(
					"--> Get List WTPartMasters from winchill number -- : ")
					.append(number).toString());
		try {
			qs = new QuerySpec(WTPartMaster.class);
			SearchCondition sc1 = new SearchCondition(WTPartMaster.class,
					WTPartMaster.NUMBER, "=", number, false);
			qs.appendWhere(sc1);
			QueryResult qr = PersistenceHelper.manager.find(qs);
			if (qr.size() > 0) {
				if (VERBOSE)
					System.out
							.println("!!!!!----getListWTPartMasters - WTPartMasters query size  - "
									+ qr.size());
				while (qr.hasMoreElements()) {
					WTPartMaster wtPartMaster = (WTPartMaster) qr.nextElement();
					if (wtPartMaster == null) {
						if (VERBOSE)
							System.out
									.println("!!!!!----getListWTPartMasters ERROR: Can not find WTPart: "
											+ number);
					} else {
						mparts.add(wtPartMaster);
					}
				}
			}
		} catch (WTException ex) {
			System.err
					.println("!!!!!----getListWTPartMasters ERROR: find WTPart is false: "
							+ number);
		}
		return mparts;
	}

	private String getOldValueAttr(AbstractValueView abstractValueView) {
		String oldVal = null;
		if (abstractValueView instanceof StringValueDefaultView) {
			oldVal = ((StringValueDefaultView) abstractValueView).getValue();
		} else if (abstractValueView instanceof ReferenceValueDefaultView) {
			// надо достать что-то типа "ПКИ/Винты"
			// oldVal = ((
			// ReferenceValueDefaultView)abstractValueView).getLiteIBAReferenceable().getIBAReferenceableDisplayString();
			// Винты
			// oldVal = ((
			// ReferenceValueDefaultView)abstractValueView).getReferenceDefinition().getName();
			// НСИ
			oldVal = "не знаю как добраться";
		}

		return oldVal;
	}

	private String setNewValue(AbstractValueView abstractValueView,
			String newVal, DefaultAttributeContainer defaultattrcontainer,
			AbstractAttributeDefinizerView abstractattributedefinizerview) {
		String result = null;
		AbstractValueView newRefValue;

		try {
			if (abstractValueView instanceof StringValueDefaultView) {
				((StringValueDefaultView) abstractValueView).setValue(newVal);
				defaultattrcontainer.updateAttributeValue(abstractValueView);
			} else if (abstractValueView instanceof ReferenceValueDefaultView) {
				newRefValue = internalCreateValue(
						abstractattributedefinizerview, newVal,
						"wt.csm.navigation.ClassificationNode");
				if (newRefValue == null) {
					result = "Не смог изменить значение в классификаторе на - "
							+ newVal;
					return result;
				}
				defaultattrcontainer.deleteAttributeValue(abstractValueView);
				defaultattrcontainer.addAttributeValue(newRefValue);
			}
		} catch (WTPropertyVetoException e) {
			result = (new StringBuilder())
					.append(
							"WTPropertyVetoException при установке нового значения атрибута в ")
					.append(newVal).toString();
			e.printStackTrace();
		} catch (IBAConstraintException e) {
			result = (new StringBuilder())
					.append(
							"IBAConstraintException при установке нового значения атрибута в ")
					.append(newVal).toString();
			e.printStackTrace();
		}

		return result;
	}

	// Достает по имени полное определение соответствующего атрибута из БД
	// WIndchill и кэширует его.
	// Метод мною немного изменен: объект кэширования .
	private AbstractAttributeDefinizerView getCachedAttributeDefinition(String s) {
		Object obj = null;

		if (definition_cache.size() > 0)
			obj = (AbstractAttributeDefinizerView) definition_cache.get(s);
		if (obj == null) {
			if (VERBOSE)
				System.out.println((new StringBuilder()).append(
						"--> Retrieving Attribute Definition from database: ")
						.append(s).toString());
			try {
				obj = IBADefinitionHelper.service
						.getAttributeDefDefaultViewByPath(s);
			} catch (IBADefinitionException ibadefinitionexception) {
				System.out
						.println((new StringBuilder())
								.append(
										"Cannot get AttributeDefDefaultView by definition path: ")
								.append(s).toString());
				return null;
			} catch (NotAuthorizedException notauthorizedexception) {
				System.out
						.println((new StringBuilder())
								.append(
										"Cannot get AttributeDefDefaultView by definition path: ")
								.append(s).toString());
				return null;
			} catch (WTException wtexception1) {
				System.out
						.println((new StringBuilder())
								.append(
										"Cannot get AttributeDefDefaultView by definition path: ")
								.append(s).toString());
				return null;
			} catch (RemoteException remoteexception) {
				System.out
						.println((new StringBuilder())
								.append(
										"Cannot get AttributeDefDefaultView by definition path: ")
								.append(s).toString());
				return null;
			}
			if (obj == null) {
				obj = DefinitionLoader.getAttributeDefinition(s);
				if (obj == null || (obj instanceof AttributeOrgNodeView)) {
					System.out
							.println((new StringBuilder())
									.append(
											"ERROR: Attribute Definition not found in database: ")
									.append(s).toString());
					return null;
				}
				try {
					obj = IBADefinitionHelper.service
							.getAttributeDefDefaultView((AttributeDefNodeView) obj);
				} catch (Exception exception) {
					System.out
							.println("Cannot convert referenceDefNodeView to AttributeDefDefaultView!");
					return null;
				}
			}
			if (obj != null) {
				definition_cache.put(s, obj);
				if (VERBOSE)
					System.out.println((new StringBuilder()).append(
							"--> Adding Attribute Definition to Cache: ")
							.append(s).toString());
			}
		} else {
			if (VERBOSE)
				System.out.println((new StringBuilder()).append(
						"--> Using Cached Attribute Definition: ").append(s)
						.toString());
		}
		return ((AbstractAttributeDefinizerView) (obj));
	}

	// метод мною урезан
	private AbstractValueView internalCreateValue(
			AbstractAttributeDefinizerView abstractattributedefinizerview,
			String s, String s1) {
		AbstractValueView abstractvalueview = null;
		if (abstractattributedefinizerview != null && VERBOSE)
			System.out.println((new StringBuilder()).append(
					"Creating value for ").append(
					abstractattributedefinizerview.getClass().getName())
					.toString());

		if (abstractattributedefinizerview instanceof StringDefView)
			abstractvalueview = newStringValue(abstractattributedefinizerview,
					s);
		else if (abstractattributedefinizerview instanceof ReferenceDefView)
			abstractvalueview = newReferenceValue(
					abstractattributedefinizerview, s, s1);
		else if (abstractattributedefinizerview instanceof IntegerDefView)
			abstractvalueview = newIntegerValue(abstractattributedefinizerview,
					s);
		else
			System.out
					.println("!!!! 1C - Windchill currently supports only String, Integer and Reference types.");

		if (abstractvalueview != null && VERBOSE)
			System.out.println((new StringBuilder()).append("Created ").append(
					abstractvalueview.getClass().getName()).toString());
		return abstractvalueview;
	}

	// метод мною изменен
	private AbstractValueView newStringValue(
			AbstractAttributeDefinizerView abstractattributedefinizerview,
			String s) {
		StringValueDefaultView stringvaluedefaultview;
		if (s == null)
			return null;
		try {
			stringvaluedefaultview = new StringValueDefaultView(
					(StringDefView) abstractattributedefinizerview, s);
		} catch (IBAValueException ibavalueexception) {
			System.out.println("!!!! Can't create string value");
			System.out.println(ibavalueexception);
			stringvaluedefaultview = null;
		}
		return stringvaluedefaultview;
	}

	public static AbstractValueView newIntegerValue(
			AbstractAttributeDefinizerView abstractattributedefinizerview,
			String s) {
		IntegerValueDefaultView integervaluedefaultview;
		long l = 0L;
		try {
			if (s != null)
				l = (Long.valueOf(s));
		} catch (NumberFormatException numberformatexception) {
			System.out.println(numberformatexception);
			return null;
		}
		try {
			integervaluedefaultview = new IntegerValueDefaultView(
					(IntegerDefView) abstractattributedefinizerview, l);
		} catch (IBAValueException ibavalueexception) {
			System.out.println("Can't create integer value");
			System.out.println(ibavalueexception);
			integervaluedefaultview = null;
		}
		return integervaluedefaultview;
	}

	// метод мною урезан
	private AbstractValueView newReferenceValue(
			AbstractAttributeDefinizerView abstractattributedefinizerview,
			String s, String s1) {
		ReferenceValueDefaultView referencevaluedefaultview;
		IBAReferenceable ibareferenceable = getIBAReferenceable(s, s1);
		if (ibareferenceable == null) {
			System.out.println((new StringBuilder()).append(
					"IBA Referenceable, class=<").append(s).append(
					">, identifier=<").append(s1).append("> not found.")
					.toString());
			return null;
		}
		try {
			referencevaluedefaultview = new ReferenceValueDefaultView(
					(ReferenceDefView) abstractattributedefinizerview);
			DefaultLiteIBAReferenceable defaultliteibareferenceable = new DefaultLiteIBAReferenceable(
					ibareferenceable);
			referencevaluedefaultview
					.setLiteIBAReferenceable(defaultliteibareferenceable);
		} catch (WTPropertyVetoException wtpropertyvetoexception) {
			System.out.println("!!!! Can't create ref value");
			wtpropertyvetoexception.printStackTrace();
			return null;
		} catch (IBAValueException ibavalueexception) {
			System.out.println("!!!!!!! Can't create ref value");
			ibavalueexception.printStackTrace();
			return null;
		}
		return referencevaluedefaultview;
	}

	// метод мною изменен
	private IBAReferenceable getIBAReferenceable(String s, String s1) {
		IBAReferenceable ibareferenceable = (IBAReferenceable) LocatorFactory
				.locateObject(s1, s); // поменял местами атрибуты !!!
		if (ibareferenceable != null && VERBOSE) {
			System.out.println((new StringBuilder()).append(
					"Found the object from ").append(s).append(
					" .IBAReference = ").append(ibareferenceable).toString());
		}
		return ibareferenceable;
	}

	private String changeStateFromDelete(WTPart existPart) throws WTException {
		String result = "OK";
		if (VERBOSE)
			System.out.println("--> Changing state back from "
					+ this.stateDelete);

		if (existPart.getCheckoutInfo().getState().getStringValue().equals(
				"wt.vc.wip.WorkInProgressState.c/i")) {
			LifeCycleHelper.service.setLifeCycleState(
					(LifeCycleManaged) existPart, State.toState("INWORK"));
			if (VERBOSE)
				System.out
						.println("--> State was changed back from Delete to In Work");
		} else
			result = "Ошибка при \"удалении\" т.к. объект взят на изменение.";

		return result;
	}

	// НЕПОНЯТНО ЗАЧЕМ ЭТО ???
	public boolean createNewViewVersion(String partNumber, String partVersion,
			String partIter, String partView, String newView) {
		if (partVersion != null && partVersion.equals(""))
			partVersion = null;
		if (partIter != null && partIter.equals(""))
			partIter = null;
		if (partView != null && partView.equals(""))
			partView = null;

		WTPart part = null;
		View nextView = null;
		try {
			nextView = ViewHelper.service.getView(newView);
			part = getPart(partNumber, partVersion, partIter, partView);
			WTPart revisedPart = (WTPart) ViewHelper.service
					.newBranchForViewAndVariations(part, nextView, null, null);
			// revisedPart = applyHardAttributes(revisedPart, nv, cmd_line);
			revisedPart = (WTPart) PersistenceHelper.manager.store(revisedPart);

		} catch (ViewException ve) {
			System.out.println("!!!!!!! Can't create newViewVersion (0)");
			ve.printStackTrace();
			return false;
		} catch (WTPropertyVetoException wtpve) {
			System.out.println("!!!!!!! Can't create newViewVersion (1)");
			wtpve.printStackTrace();
			return false;
		} catch (WTException wte) {
			System.out.println("!!!!!!! Can't create newViewVersion (2)");
			wte.printStackTrace();
			return false;
		}
		return true;
	}

	@Deprecated
	private WTPart getPart(String number, String version, String iteration,
			String view) throws WTException {
		// WTPart cachedPart = getCachedPart(number, version, iteration, view,
		// variation1, variation2, org_id, org_name);
		// if( cachedPart != null ) {
		// return cachedPart;
		// }

		LatestConfigSpec configSpec = null;
		QuerySpec qs = new QuerySpec(WTPart.class);
		qs.appendWhere(new SearchCondition(WTPart.class, WTPart.NUMBER,
				SearchCondition.EQUAL, number.toUpperCase(), false));

		if (view != null) {
			View viewObj = ViewHelper.service.getView(view);
			qs.appendAnd();
			qs.appendWhere(new SearchCondition(WTPart.class,
					ViewManageable.VIEW + "." + ObjectReference.KEY,
					SearchCondition.EQUAL, PersistenceHelper
							.getObjectIdentifier(viewObj)));
		}
		if (version != null) {
			qs.appendAnd();
			qs.appendWhere(new SearchCondition(WTPart.class,
					Versioned.VERSION_INFO + "." + VersionInfo.IDENTIFIER + "."
							+ "versionId", SearchCondition.EQUAL, version,
					false));
			if (iteration != null) {
				qs.appendAnd();
				qs.appendWhere(new SearchCondition(WTPart.class,
						Iterated.ITERATION_INFO + "."
								+ IterationInfo.IDENTIFIER + "."
								+ "iterationId", SearchCondition.EQUAL,
						iteration, false));
			} else {
				// iteration==null
				qs.appendAnd();
				qs.appendWhere(new SearchCondition(WTPart.class,
						Iterated.ITERATION_INFO + "." + IterationInfo.LATEST,
						SearchCondition.IS_TRUE));
			}
		} else {
			// version == null && assume iteration == null
			configSpec = new LatestConfigSpec();
			configSpec.appendSearchCriteria(qs);
		}

		QueryResult qr = PersistenceHelper.manager.find(qs);
		if (VERBOSE) {
			System.out.println("!!!! Query found " + qr.size()
					+ " matching parts.");
		}
		if (configSpec != null) {
			qr = configSpec.process(qr);
		}
		if (VERBOSE) {
			if (configSpec != null) {
				System.out.println("!!!! Query filtered by ConfigSpec found "
						+ qr.size() + " matching parts.");
			}
		}
		if (qr.size() == 1) {
			WTPart part = (WTPart) qr.nextElement();
			if (WorkInProgressHelper.isCheckedOut(part)) {
				throw new WTException("Часть взята на изменение");
			}
			return part;
		} else if (qr.size() > 1) {
			String msg = "Found " + qr.size() + " parts that match number="
					+ number + " version=" + version + " iteration="
					+ iteration + " view=" + view + "."
					+ " Expecting only one part.";
			throw new WTException(msg);
		} else { // qr.size() == 0
			return null;
		}
	}

	/**
	 * find wtpart from oid WTPartMaster
	 * 
	 * @author Bykovskaya
	 * @param oid wtPartMaster
	 * @return wtPart
	 */
	private WTPart findChangedPartFromID(String oid) throws QueryException,
			WTException {
		WTPart part = null;
		String idMaster = "wt.part.WTPartMaster:" + oid;
		ReferenceFactory refFact = new ReferenceFactory();
		WTReference wtRef = (WTReference) refFact.getReference(idMaster);
		WTPartMaster partMaster = (WTPartMaster) wtRef.getObject();
		QueryResult qr = VersionControlHelper.service.allVersionsOf(partMaster);
		boolean latestIteration = false;
		while ((qr.hasMoreElements()) && (!latestIteration)) {
			Object p = qr.nextElement();
			if (p instanceof WTPart) {
				if (VERBOSE)
					System.out.println("Found Part - " + ((WTPart) p).getName()
							+ "   " + ((WTPart) p).getNumber() + "  "
							+ ((WTPart) p).isLatestIteration());
				WTPart tempPart = (WTPart) p;
				latestIteration = tempPart.isLatestIteration();
				if (latestIteration) {
					part = tempPart;
					System.out.println("!!!---latestIteration) - "
							+ part.getName() + "   " + part.getNumber());
				}
			} else {
				part = null; // // на всякий случай. // Было у Славы
				break;
			}
		}
		return part;
	}

}
