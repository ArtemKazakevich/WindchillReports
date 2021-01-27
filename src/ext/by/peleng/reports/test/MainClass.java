package ext.by.peleng.reports.test;

import org.apache.log4j.BasicConfigurator;
import wt.method.RemoteMethodServer;
import java.net.MalformedURLException;
import java.net.URL;

public class MainClass {

    public static void main(String[] args) {

        BasicConfigurator.configure();
        try {

            String partNumber = "6139.30.01.200";
            RemoteMethodServer remotemethodserver = MainClass.getMS();
            remotemethodserver.setUserName("11772");
            remotemethodserver.setPassword("sasha260894");
            Class[] argTypes = new Class[1];
            argTypes[0] = String.class;
            Object[] argValues = new Object[1];
            argValues[0] = partNumber;

            Object object = null;

            object = remotemethodserver.invoke("getProductStructureByPartNumber",
                    "ext.by.peleng.reports.test.WindchillRmiTest",
                    null,
                    argTypes,
                    argValues);

            System.out.println(object);

        }catch (Exception e) {

            System.err.println(e.getMessage());

        } finally {

            System.exit(0);

        }

    }

    public static RemoteMethodServer getMS() throws MalformedURLException {

        URL remote_url = new URL("http://sandbox.peleng.by/Windchill/");

        return RemoteMethodServer.getInstance(remote_url);

    }

}
