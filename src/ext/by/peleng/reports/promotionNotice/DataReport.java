package ext.by.peleng.reports.promotionNotice;

public class DataReport {
    private String numberPN;
    private String namePN;
    private String creatorPN;
    private String dateOfCreationPN;
    private String statePN;
    private String typeWT;
    private String numberWT;
    private String nameWT;
    private String versionWT;
    private String currentStateWT;
    private String nameVEA;
    private String roleVEA;
    private String userVEA;
    private String voteVEA;
    private String userCommentVEA;
    private String deadLineVEA;

    public DataReport() {
    }

    public DataReport(String numberPN, String namePN, String creatorPN, String dateOfCreationPN, String statePN,
                      String typeWT, String numberWT, String nameWT, String versionWT, String currentStateWT) {
        this.numberPN = numberPN;
        this.namePN = namePN;
        this.creatorPN = creatorPN;
        this.dateOfCreationPN = dateOfCreationPN;
        this.statePN = statePN;
        this.typeWT = typeWT;
        this.numberWT = numberWT;
        this.nameWT = nameWT;
        this.versionWT = versionWT;
        this.currentStateWT = currentStateWT;
    }

    public DataReport(String numberPN, String namePN, String creatorPN, String dateOfCreationPN, String statePN,
                      String nameVEA, String roleVEA, String userVEA, String voteVEA, String userCommentVEA, String deadLineVEA) {
        this.numberPN = numberPN;
        this.namePN = namePN;
        this.creatorPN = creatorPN;
        this.dateOfCreationPN = dateOfCreationPN;
        this.statePN = statePN;
        this.nameVEA = nameVEA;
        this.roleVEA = roleVEA;
        this.userVEA = userVEA;
        this.voteVEA = voteVEA;
        this.userCommentVEA = userCommentVEA;
        this.deadLineVEA = deadLineVEA;
    }

    public String getNumberPN() {
        return numberPN;
    }

    public void setNumberPN(String numberPN) {
        this.numberPN = numberPN;
    }

    public String getNamePN() {
        return namePN;
    }

    public void setNamePN(String namePN) {
        this.namePN = namePN;
    }

    public String getCreatorPN() {
        return creatorPN;
    }

    public void setCreatorPN(String creatorPN) {
        this.creatorPN = creatorPN;
    }

    public String getDateOfCreationPN() {
        return dateOfCreationPN;
    }

    public void setDateOfCreationPN(String dateOfCreationPN) {
        this.dateOfCreationPN = dateOfCreationPN;
    }

    public String getStatePN() {
        return statePN;
    }

    public void setStatePN(String statePN) {
        this.statePN = statePN;
    }

    public String getTypeWT() {
        return typeWT;
    }

    public void setTypeWT(String typeWT) {
        this.typeWT = typeWT;
    }

    public String getNumberWT() {
        return numberWT;
    }

    public void setNumberWT(String numberWT) {
        this.numberWT = numberWT;
    }

    public String getNameWT() {
        return nameWT;
    }

    public void setNameWT(String nameWT) {
        this.nameWT = nameWT;
    }

    public String getVersionWT() {
        return versionWT;
    }

    public void setVersionWT(String versionWT) {
        this.versionWT = versionWT;
    }

    public String getCurrentStateWT() {
        return currentStateWT;
    }

    public void setCurrentStateWT(String currentStateWT) {
        this.currentStateWT = currentStateWT;
    }

    public String getNameVEA() {
        return nameVEA;
    }

    public void setNameVEA(String nameVEA) {
        this.nameVEA = nameVEA;
    }

    public String getRoleVEA() {
        return roleVEA;
    }

    public void setRoleVEA(String roleVEA) {
        this.roleVEA = roleVEA;
    }

    public String getUserVEA() {
        return userVEA;
    }

    public void setUserVEA(String userVEA) {
        this.userVEA = userVEA;
    }

    public String getVoteVEA() {
        return voteVEA;
    }

    public void setVoteVEA(String voteVEA) {
        this.voteVEA = voteVEA;
    }

    public String getUserCommentVEA() {
        return userCommentVEA;
    }

    public void setUserCommentVEA(String userCommentVEA) {
        this.userCommentVEA = userCommentVEA;
    }

    public String getDeadLineVEA() {
        return deadLineVEA;
    }

    public void setDeadLineVEA(String deadLineVEA) {
        this.deadLineVEA = deadLineVEA;
    }

}
