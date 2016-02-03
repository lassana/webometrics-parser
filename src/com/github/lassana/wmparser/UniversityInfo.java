package com.github.lassana.wmparser;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Nikolai Doronin {@literal <lassana.nd@gmail.com>}
 * @since 2/3/16.
 */
public class UniversityInfo {

    private String worldRank;
    private String university;
    private String universitySite;
    private String presenceRank;
    private String impactRank;
    private String openessRank;
    private String excellenceRank;
    private String region;
    private String country;
    private Map<String, String> topicTrust;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getOpenessRank() {
        return openessRank;
    }

    public void setOpenessRank(String openessRank) {
        this.openessRank = openessRank;
    }

    public String getExcellenceRank() {
        return excellenceRank;
    }

    public void setExcellenceRank(String excellenceRank) {
        this.excellenceRank = excellenceRank;
    }

    public String getImpactRank() {
        return impactRank;
    }

    public void setImpactRank(String impactRank) {
        this.impactRank = impactRank;
    }

    public String getPresenceRank() {
        return presenceRank;
    }

    public void setPresenceRank(String presenceRank) {
        this.presenceRank = presenceRank;
    }

    public String getUniversitySite() {
        return universitySite;
    }

    public void setUniversitySite(String universitySite) {
        this.universitySite = universitySite;
    }

    public String getUniversity() {
        return university;
    }

    public void setUniversity(String university) {
        this.university = university;
    }

    public String getWorldRank() {
        return worldRank;
    }

    public void setWorldRank(String worldRank) {
        this.worldRank = worldRank;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UniversityInfo that = (UniversityInfo) o;

        if (!country.equals(that.country)) return false;
        if (!excellenceRank.equals(that.excellenceRank)) return false;
        if (!impactRank.equals(that.impactRank)) return false;
        if (!openessRank.equals(that.openessRank)) return false;
        if (!presenceRank.equals(that.presenceRank)) return false;
        if (!region.equals(that.region)) return false;
        if (!university.equals(that.university)) return false;
        if (!universitySite.equals(that.universitySite)) return false;
        if (!worldRank.equals(that.worldRank)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = worldRank.hashCode();
        result = 31 * result + university.hashCode();
        result = 31 * result + universitySite.hashCode();
        result = 31 * result + presenceRank.hashCode();
        result = 31 * result + impactRank.hashCode();
        result = 31 * result + openessRank.hashCode();
        result = 31 * result + excellenceRank.hashCode();
        result = 31 * result + region.hashCode();
        result = 31 * result + country.hashCode();
        return result;
    }

    public Map<String, String> getTopicTrust() {
        return topicTrust;
    }

    public void setTopicTrust(Map<String, String> topicTrust) {
        this.topicTrust = topicTrust;
    }

    public List<String> toList() {
        return
                Arrays.asList(
                        getWorldRank(),
                        getUniversity(),
                        getUniversitySite(),
                        getPresenceRank(),
                        getImpactRank(),
                        getOpenessRank(),
                        getExcellenceRank(),
                        getCountry(),
                        getRegion()
                );
    }
}