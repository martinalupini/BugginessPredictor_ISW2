package it.lupini.model;

import org.eclipse.jgit.revwalk.RevCommit;

import java.util.ArrayList;
import java.util.List;

public class JavaFile {
    private String name;
    private String content;
    private Release release;   //release where the class is created

    private final List<RevCommit> commits;    //list of commits that modified the class' content

    private List<RevCommit> fixCommits;

    private List<Integer> locAddedList;

    private List<Integer> locRemovedList;

    //METRICS
    private Integer loc;
    private Integer locTouched;
    private Integer nRevisions;  //number of commits that touch the class
    private Integer nFix;
    private Integer nAuth;
    private Integer locAdded;
    private Integer maxLocAdded;
    private Integer avgLocAdded;
    private Integer churn;
    private Integer maxChurn;
    private Integer avgChurn;

    private Integer commentLines;

    //prevision metric
    private boolean buggyness;



    public JavaFile(String name, Release release, String content) {

        this.name = name;
        this.release = release;
        this.content = content;
        this.buggyness = false;
        this.nRevisions = 0;
        this.nAuth = 0;
        this.loc = 0;
        this.locAdded = 0;
        this.locTouched = 0;
        this.avgLocAdded = 0;
        this.locAddedList = new ArrayList<>();
        this.locRemovedList = new ArrayList<>();
        this.churn = 0;
        this.avgChurn = 0;
        this.commits = new ArrayList<>();
        this.commentLines = 0;
        this.fixCommits = new ArrayList<>();
    }


    public String getName() {
        return name;
    }

    public boolean getBuggyness() {
        return buggyness;
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

    public Integer getNRevisions() {
        return nRevisions;
    }

    public List<Integer> getLocAddedList() {
        return locAddedList;
    }
    public int getNAuth() {
        return nAuth;
    }

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

    public void setChurn(Integer churn) {
        this.churn = churn;
    }

    public void setNRevisions(Integer nRevisions) {
        this.nRevisions = nRevisions;
    }

    public void setMaxLocAdded(Integer maxLOCAdded) {
        this.maxLocAdded = maxLOCAdded;
    }

    public void setAvgLOCAdded(Integer avgLOCAdded) {
        this.avgLocAdded = avgLOCAdded;
    }


    public Integer getNFix() {
        return nFix;
    }

    public void setNFix(Integer nFix) {
        this.nFix = nFix;
    }

    public void setNAuth(int nAuth) {
        this.nAuth = nAuth;
    }
    public Integer getMAXChurn() {
        return maxChurn;
    }

    public void setMaxChurn(Integer maxChurn) {
        this.maxChurn = maxChurn;
    }

    public Integer getAVGChurn() {
        return avgChurn;
    }

    public void setAvgChurn(Integer aVGChurn) {
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

    public List<Integer> getLocRemovedList() {
        return locRemovedList;
    }


    public void addLocAdded(int loc){
        this.locAddedList.add(loc);
    }

    public void addLocRemoved(int loc){
        this.locRemovedList.add(loc);
    }

    public void sumLocAdded(int loc){
        this.locAdded += loc;
    }

    public void sumChurn(int churn){ this.churn += churn;}


    public List<RevCommit> getFixCommits() {
        return fixCommits;
    }

    public void addFixCommit(RevCommit fixCommit) {
        this.fixCommits.add(fixCommit);
    }

    public void addLOCTouched(int touchedLinesOfCode) {
        this.locTouched += touchedLinesOfCode;
    }
}
