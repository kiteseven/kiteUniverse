package org.kiteseven.kiteuniverse.pojo.vo.content;

import java.util.ArrayList;
import java.util.List;

/**
 * Frontend content payload used by the community homepage.
 */
public class HomePageVO {

    /**
     * Hero section content displayed at the top of the homepage.
     */
    private Hero hero;

    /**
     * Key metrics displayed under the hero copy.
     */
    private List<MetricCard> heroMetrics = new ArrayList<>();

    /**
     * Featured content cards displayed in the main recommendation area.
     */
    private List<TopicCard> featuredTopics = new ArrayList<>();

    /**
     * Shortcut sections displayed in the right-side panel.
     */
    private List<QuickSection> quickSections = new ArrayList<>();

    /**
     * Timeline items displayed in the community bulletin area.
     */
    private List<TimelineItem> timeline = new ArrayList<>();

    /**
     * Snapshot metrics displayed in the right-side spotlight panel.
     */
    private List<MetricCard> moments = new ArrayList<>();

    public Hero getHero() {
        return hero;
    }

    public void setHero(Hero hero) {
        this.hero = hero;
    }

    public List<MetricCard> getHeroMetrics() {
        return heroMetrics;
    }

    public void setHeroMetrics(List<MetricCard> heroMetrics) {
        this.heroMetrics = heroMetrics;
    }

    public List<TopicCard> getFeaturedTopics() {
        return featuredTopics;
    }

    public void setFeaturedTopics(List<TopicCard> featuredTopics) {
        this.featuredTopics = featuredTopics;
    }

    public List<QuickSection> getQuickSections() {
        return quickSections;
    }

    public void setQuickSections(List<QuickSection> quickSections) {
        this.quickSections = quickSections;
    }

    public List<TimelineItem> getTimeline() {
        return timeline;
    }

    public void setTimeline(List<TimelineItem> timeline) {
        this.timeline = timeline;
    }

    public List<MetricCard> getMoments() {
        return moments;
    }

    public void setMoments(List<MetricCard> moments) {
        this.moments = moments;
    }

    /**
     * Hero section payload.
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
         * Primary call-to-action label.
         */
        private String primaryActionLabel;

        /**
         * Primary call-to-action link.
         */
        private String primaryActionLink;

        /**
         * Secondary call-to-action label.
         */
        private String secondaryActionLabel;

        /**
         * Secondary call-to-action link.
         */
        private String secondaryActionLink;

        /**
         * Alt text used by the frontend visual asset.
         */
        private String visualAlt;

        /**
         * Title for the first floating hero card.
         */
        private String floatingTitle;

        /**
         * Value for the first floating hero card.
         */
        private String floatingValue;

        /**
         * Description for the first floating hero card.
         */
        private String floatingDescription;

        /**
         * Title for the second floating hero card.
         */
        private String secondaryFloatingTitle;

        /**
         * Value for the second floating hero card.
         */
        private String secondaryFloatingValue;

        /**
         * Description for the second floating hero card.
         */
        private String secondaryFloatingDescription;

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

        public String getPrimaryActionLabel() {
            return primaryActionLabel;
        }

        public void setPrimaryActionLabel(String primaryActionLabel) {
            this.primaryActionLabel = primaryActionLabel;
        }

        public String getPrimaryActionLink() {
            return primaryActionLink;
        }

        public void setPrimaryActionLink(String primaryActionLink) {
            this.primaryActionLink = primaryActionLink;
        }

        public String getSecondaryActionLabel() {
            return secondaryActionLabel;
        }

        public void setSecondaryActionLabel(String secondaryActionLabel) {
            this.secondaryActionLabel = secondaryActionLabel;
        }

        public String getSecondaryActionLink() {
            return secondaryActionLink;
        }

        public void setSecondaryActionLink(String secondaryActionLink) {
            this.secondaryActionLink = secondaryActionLink;
        }

        public String getVisualAlt() {
            return visualAlt;
        }

        public void setVisualAlt(String visualAlt) {
            this.visualAlt = visualAlt;
        }

        public String getFloatingTitle() {
            return floatingTitle;
        }

        public void setFloatingTitle(String floatingTitle) {
            this.floatingTitle = floatingTitle;
        }

        public String getFloatingValue() {
            return floatingValue;
        }

        public void setFloatingValue(String floatingValue) {
            this.floatingValue = floatingValue;
        }

        public String getFloatingDescription() {
            return floatingDescription;
        }

        public void setFloatingDescription(String floatingDescription) {
            this.floatingDescription = floatingDescription;
        }

        public String getSecondaryFloatingTitle() {
            return secondaryFloatingTitle;
        }

        public void setSecondaryFloatingTitle(String secondaryFloatingTitle) {
            this.secondaryFloatingTitle = secondaryFloatingTitle;
        }

        public String getSecondaryFloatingValue() {
            return secondaryFloatingValue;
        }

        public void setSecondaryFloatingValue(String secondaryFloatingValue) {
            this.secondaryFloatingValue = secondaryFloatingValue;
        }

        public String getSecondaryFloatingDescription() {
            return secondaryFloatingDescription;
        }

        public void setSecondaryFloatingDescription(String secondaryFloatingDescription) {
            this.secondaryFloatingDescription = secondaryFloatingDescription;
        }
    }

    /**
     * Small metric card payload.
     */
    public static class MetricCard {

        /**
         * Metric title displayed on the card.
         */
        private String title;

        /**
         * Main metric value.
         */
        private String value;

        /**
         * Supporting description.
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
     * Featured topic card payload.
     */
    public static class TopicCard {

        /**
         * Topic badge text.
         */
        private String badge;

        /**
         * Topic title.
         */
        private String title;

        /**
         * Topic excerpt.
         */
        private String excerpt;

        /**
         * Topic author or source.
         */
        private String author;

        /**
         * Topic meta information.
         */
        private String meta;

        /**
         * Small stat chips displayed under the topic card.
         */
        private List<String> stats = new ArrayList<>();

        /**
         * Frontend route path used to open the post detail page.
         */
        private String link;

        public String getBadge() {
            return badge;
        }

        public void setBadge(String badge) {
            this.badge = badge;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getExcerpt() {
            return excerpt;
        }

        public void setExcerpt(String excerpt) {
            this.excerpt = excerpt;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getMeta() {
            return meta;
        }

        public void setMeta(String meta) {
            this.meta = meta;
        }

        public List<String> getStats() {
            return stats;
        }

        public void setStats(List<String> stats) {
            this.stats = stats;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }
    }

    /**
     * Shortcut section payload.
     */
    public static class QuickSection {

        /**
         * Shortcut section title.
         */
        private String title;

        /**
         * Shortcut links rendered as pills.
         */
        private List<QuickLink> items = new ArrayList<>();

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public List<QuickLink> getItems() {
            return items;
        }

        public void setItems(List<QuickLink> items) {
            this.items = items;
        }
    }

    /**
     * Small shortcut link payload.
     */
    public static class QuickLink {

        /**
         * Shortcut label text.
         */
        private String label;

        /**
         * Shortcut target link.
         */
        private String link;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }
    }

    /**
     * Timeline bulletin item payload.
     */
    public static class TimelineItem {

        /**
         * Timeline timestamp label.
         */
        private String time;

        /**
         * Timeline title.
         */
        private String title;

        /**
         * Timeline description.
         */
        private String description;

        /**
         * Frontend route path used to open the linked post.
         */
        private String link;

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
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

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }
    }
}
