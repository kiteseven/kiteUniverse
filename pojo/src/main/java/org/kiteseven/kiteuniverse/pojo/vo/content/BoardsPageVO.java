package org.kiteseven.kiteuniverse.pojo.vo.content;

import java.util.ArrayList;
import java.util.List;

/**
 * Frontend content payload used by the boards page.
 */
public class BoardsPageVO {

    /**
     * Hero section content displayed at the top of the boards page.
     */
    private Hero hero;

    /**
     * Board-group cards displayed in the main content area.
     */
    private List<BoardCard> boardGroups = new ArrayList<>();

    /**
     * Guidance notices displayed in the side panel.
     */
    private List<String> notices = new ArrayList<>();

    /**
     * Overview metrics displayed in the side panel.
     */
    private List<MetricCard> overviewCards = new ArrayList<>();

    public Hero getHero() {
        return hero;
    }

    public void setHero(Hero hero) {
        this.hero = hero;
    }

    public List<BoardCard> getBoardGroups() {
        return boardGroups;
    }

    public void setBoardGroups(List<BoardCard> boardGroups) {
        this.boardGroups = boardGroups;
    }

    public List<String> getNotices() {
        return notices;
    }

    public void setNotices(List<String> notices) {
        this.notices = notices;
    }

    public List<MetricCard> getOverviewCards() {
        return overviewCards;
    }

    public void setOverviewCards(List<MetricCard> overviewCards) {
        this.overviewCards = overviewCards;
    }

    /**
     * Boards-page hero payload.
     */
    public static class Hero {

        /**
         * Small title displayed above the hero heading.
         */
        private String eyebrow;

        /**
         * Main hero heading.
         */
        private String title;

        /**
         * Hero description paragraph.
         */
        private String description;

        /**
         * Hero metrics displayed on the right.
         */
        private List<MetricCard> metrics = new ArrayList<>();

        public String getEyebrow() {
            return eyebrow;
        }

        public void setEyebrow(String eyebrow) {
            this.eyebrow = eyebrow;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<MetricCard> getMetrics() {
            return metrics;
        }

        public void setMetrics(List<MetricCard> metrics) {
            this.metrics = metrics;
        }
    }

    /**
     * Boards page metric card payload.
     */
    public static class MetricCard {

        /**
         * Metric title.
         */
        private String title;

        /**
         * Metric value.
         */
        private String value;

        /**
         * Optional supporting description.
         */
        private String description;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    /**
     * Board-group card payload.
     */
    public static class BoardCard {

        /**
         * Board tag label.
         */
        private String tag;

        /**
         * Board title.
         */
        private String title;

        /**
         * Board description.
         */
        private String description;

        /**
         * Board statistic chips.
         */
        private List<String> stats = new ArrayList<>();

        /**
         * Latest update hint.
         */
        private String update;

        /**
         * Call-to-action label.
         */
        private String actionLabel;

        /**
         * Call-to-action link.
         */
        private String actionLink;

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<String> getStats() {
            return stats;
        }

        public void setStats(List<String> stats) {
            this.stats = stats;
        }

        public String getUpdate() {
            return update;
        }

        public void setUpdate(String update) {
            this.update = update;
        }

        public String getActionLabel() {
            return actionLabel;
        }

        public void setActionLabel(String actionLabel) {
            this.actionLabel = actionLabel;
        }

        public String getActionLink() {
            return actionLink;
        }

        public void setActionLink(String actionLink) {
            this.actionLink = actionLink;
        }
    }
}
