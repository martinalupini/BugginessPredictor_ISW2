package it.Lupini.model;

import org.eclipse.jgit.revwalk.RevCommit;

import java.util.ArrayList;
import java.util.List;

public class JavaFile {
    private String name;
    private String content;
    private Release release;

    private final List<RevCommit> commits;

    //???????????????????
    private List<String> oldPaths;
    private List<Integer> churnList;

    private List<Integer> locAddedList;

    //METRICS
    private Integer loc;
    private Integer locTouched;
    private Integer nr;
    private Integer nFix;
    private List<String> nAuth;
    private Integer locAdded;
    private Integer maxLocAdded;
    private Integer avgLocAdded;
    private Integer churn;
    private Integer maxChurn;
    private Integer avgChurn;

    private Integer commentLines;

    //prevision metric
    private boolean buggyness;



    public JavaFile(String name) {

        this.name = name;
        this.buggyness = false;
        this.nr = 0;
        this.nAuth = new ArrayList<>();
        this.loc = 0;
        this.locAdded = 0;
        this.locTouched = 0;
        this.avgLocAdded = 0;
        this.locAddedList = new ArrayList<>();
        this.churn = 0;
        this.avgChurn = 0;
        this.churnList = new ArrayList<>();
        this.commits = new ArrayList<>();
        this.commentLines = 0;
    }

    // get
    public String getName() {
        return name;
    }

    public boolean getBuggyness() {
        return buggyness;
    }

    public List<String> getoldPaths() {
        return oldPaths;
    }

    public Integer getLoc() {
        return loc;
    }

    public Integer getLOCtouched() {
        return locTouched;
    }

    public Integer getLOCadded() {
        return locAdded;
    }

    public List<RevCommit> getCommits(){
        return this.commits;
    }

    public Integer getChurn() {
        return churn;
    }



    public Integer getMaxlocAdded() {
        return maxLocAdded;
    }

    public Integer getAvglocAdded() {
        return avgLocAdded;
    }

    public Integer getNr() {
        return nr;
    }
    public List<Integer> getChurnList() {
        return churnList;
    }

    public List<Integer> getLocAddedList() {
        return locAddedList;
    }
    public List<String> getNAuth() {
        return nAuth;
    }

    // set

    public void addCommit(RevCommit commit){
        this.commits.add(commit);
    }
    public void setName(String name) {
        this.name = name;
    }

    public void setBuggyness(boolean buggyness) {
        this.buggyness = buggyness;
    }

    public void setLoc(Integer loc) {
        this.loc = loc;
    }

    public void setLOCadded(Integer locAdded) {
        this.locAdded = locAdded;
    }

    public void setLOCtouched(Integer locTouched) {
        this.locTouched = locTouched;
    }

    public void setChurn(Integer churn) {
        this.churn = churn;
    }

    public void setNr(Integer nr) {
        this.nr = nr;
    }

    public void setMaxLocAdded(Integer maxLOCAdded) {
        this.maxLocAdded = maxLOCAdded;
    }

    public void setAvgLOCAdded(Integer avgLOCAdded) {
        this.avgLocAdded = avgLOCAdded;
    }

    public void setOldPaths(List<String> oldPaths) {
        this.oldPaths = oldPaths;
    }

    public Integer getnFix() {
        return nFix;
    }

    public void setnFix(Integer nFix) {
        this.nFix = nFix;
    }

    public void setNAuth(List<String> nAuth) {
        this.nAuth = nAuth;
    }
    public void setLocAddedList(List<Integer> locAddedList) {
        this.locAddedList = locAddedList;
    }
    public void setChurnList(List<Integer> churnList) {
        this.churnList = churnList;
    }

    public Integer getMAXChurn() {
        return maxChurn;
    }

    public void setMAXChurn(Integer maxChurn) {
        this.maxChurn = maxChurn;
    }

    public Integer getAVGChurn() {
        return avgChurn;
    }

    public void setAVGChurn(Integer aVGChurn) {
        this.avgChurn = aVGChurn;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Release getRelease() {
        return release;
    }

    public void setRelease(Release release) {
        this.release = release;
    }

    public Integer getCommentLines() {
        return commentLines;
    }

    public void setCommentLines(Integer commentLines) {
        this.commentLines = commentLines;
    }
}
