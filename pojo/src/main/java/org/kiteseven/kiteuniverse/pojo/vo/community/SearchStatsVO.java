package org.kiteseven.kiteuniverse.pojo.vo.community;

import java.util.List;

/**
 * 搜索统计视图对象，供运营看板使用。
 */
public class SearchStatsVO {

    /**
     * 热门搜索词列表（词 + 搜索次数）。
     */
    private List<TermCount> hotKeywords;

    /**
     * 总搜索次数。
     */
    private long totalSearches;

    /**
     * 无结果搜索次数。
     */
    private long zeroResultSearches;

    /**
     * 无结果率（百分比，保留一位小数）。
     */
    private double zeroResultRate;

    public List<TermCount> getHotKeywords() { return hotKeywords; }
    public void setHotKeywords(List<TermCount> hotKeywords) { this.hotKeywords = hotKeywords; }

    public long getTotalSearches() { return totalSearches; }
    public void setTotalSearches(long totalSearches) { this.totalSearches = totalSearches; }

    public long getZeroResultSearches() { return zeroResultSearches; }
    public void setZeroResultSearches(long zeroResultSearches) { this.zeroResultSearches = zeroResultSearches; }

    public double getZeroResultRate() { return zeroResultRate; }
    public void setZeroResultRate(double zeroResultRate) { this.zeroResultRate = zeroResultRate; }

    /**
     * 词条 + 出现次数。
     */
    public static class TermCount {
        private String term;
        private long count;

        public TermCount(String term, long count) {
            this.term = term;
            this.count = count;
        }

        public String getTerm() { return term; }
        public long getCount() { return count; }
    }
}
