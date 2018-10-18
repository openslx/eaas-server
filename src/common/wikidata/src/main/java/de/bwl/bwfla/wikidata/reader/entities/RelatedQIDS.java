package de.bwl.bwfla.wikidata.reader.entities;

import java.util.HashSet;

public class RelatedQIDS {
    String qid;
    HashSet<String> following = new HashSet<>();
    HashSet<String> followedBy = new HashSet<>();


    public RelatedQIDS(String qid) {
        this.qid = qid;
    }

    public RelatedQIDS(String qid, HashSet<String> following, HashSet<String> followedBy) {
        this.qid = qid;
        this.following = following;
        this.followedBy = followedBy;
    }

    public void addFollowingQID(String qID){
        following.add(qID);
    }

    public void addFollowedQID(String qID){
        followedBy.add(qID);
    }

    public String getQid() {
        return qid;
    }

    public HashSet<String> getFollowing() {
        return following;
    }

    public HashSet<String> getFollowedBy() {
        return followedBy;
    }
}
