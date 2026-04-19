package com.MCF.backend.config;

import com.MCF.backend.model.Category;
import com.MCF.backend.model.Issue;
import com.MCF.backend.model.User;
import com.MCF.backend.repository.CategoryRepository;
import com.MCF.backend.repository.IssueRepository;
import com.MCF.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
public class DefaultContentSeeder implements CommandLineRunner {

    private static final String LEGACY_COMMUNITY_THREAD_TITLE = "Community check-in thread";
    private static final String EVENT_STYLE_COMMUNITY_TITLE = "Community check-in event";
    private static final String EVENT_STYLE_COMMUNITY_DESCRIPTION = "Share your neighborhood event plans, meetup details, and community updates.";
    private static final String DEFAULT_ISSUE_STATUS = "OPEN";
    private static final String DEFAULT_ADMIN_EMAIL = "admin@admin.com";
    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin";

    private final CategoryRepository categoryRepository;
    private final IssueRepository issueRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.announcements.category-id:1}")
    private long announcementsCategoryId;

    @Value("${app.seed.default-content:true}")
    private boolean seedDefaultContent;

    public DefaultContentSeeder(
            CategoryRepository categoryRepository,
            IssueRepository issueRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.categoryRepository = categoryRepository;
        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedDefaultContent) {
            return;
        }

        ensureDefaultAdminUser();

        Category announcements = findOrCreateCategory("Announcements");
        Category community = findOrCreateCategory("Community");
        Category guides = findOrCreateCategory("Guides");
        findOrCreateCategory("Neighborhood");

        migrateLegacyCommunityThreadTitle(community);

        User systemUser = findOrCreateSystemUser();

        Category announcementsCategory = categoryRepository.findById(announcementsCategoryId).orElse(announcements);

        seedAnnouncements(systemUser, announcementsCategory);
        seedCommunityBulletins(systemUser, community);
        seedGuides(systemUser, guides);
    }

        private void seedAnnouncements(User systemUser, Category announcementsCategory) {
        createIssueIfMissing(
            systemUser,
            announcementsCategory,
            "Welcome to Monterey Forum",
            "Welcome! Use this space for official updates and community news.",
            null
        );

        createIssueIfMissing(
            systemUser,
            announcementsCategory,
            "Farmers Market Hours Extended Through Summer",
            "Good news: the Old Monterey Marketplace will remain open until 4:00 PM on Saturdays through September. " +
                "Bring reusable bags and support local growers.",
            "Old Monterey"
        );

        createIssueIfMissing(
            systemUser,
            announcementsCategory,
            "Neighborhood Preparedness Meeting - May 2",
            "Join the city emergency preparedness team on May 2 at 6:30 PM at the Seaside Community Center. " +
                "We will cover evacuation routes, alerts, and household readiness checklists.",
            "Seaside Community Center"
        );

        createIssueIfMissing(
            systemUser,
            announcementsCategory,
            "Roadwork Notice: Del Monte Ave Lane Closures",
            "Public works crews will repair storm drains on Del Monte Ave next week. " +
                "Expect single-lane closures between 9:00 AM and 3:00 PM Monday through Thursday.",
            "Del Monte Ave"
        );
        }

        private void seedCommunityBulletins(User systemUser, Category communityCategory) {
        createIssueIfMissing(
            systemUser,
            communityCategory,
            EVENT_STYLE_COMMUNITY_TITLE,
            EVENT_STYLE_COMMUNITY_DESCRIPTION,
            null
        );

        createIssueIfMissing(
            systemUser,
            communityCategory,
            "Weekend Beach Cleanup at Del Monte",
            "We are meeting this Saturday at 9:00 AM near the volleyball courts for a community cleanup. " +
                "Gloves and bags provided. Families and students welcome.",
            "Del Monte Beach"
        );

        createIssueIfMissing(
            systemUser,
            communityCategory,
            "Downtown Art Walk and Live Music Night",
            "Local artists and student musicians will be featured Friday from 5:30 PM to 8:30 PM. " +
                "Shops stay open late and food trucks will line Alvarado Street.",
            "Alvarado Street"
        );

        createIssueIfMissing(
            systemUser,
            communityCategory,
            "Family Board Game Meetup",
            "Bring your favorite board game to the Marina branch library this Sunday at 2:00 PM. " +
                "New players are encouraged and light snacks will be available.",
            "Marina Library"
        );
        }

        private void seedGuides(User systemUser, Category guidesCategory) {
        createIssueIfMissing(
            systemUser,
            guidesCategory,
            "How to Report City Service Requests Quickly",
            "Need a pothole repaired, streetlight fixed, or graffiti removed? " +
                "Use the city service portal, include a clear location pin, and attach one photo for fastest routing.",
            "Monterey"
        );

        createIssueIfMissing(
            systemUser,
            guidesCategory,
            "Beginner's Guide to Monterey Transit",
            "This tutorial explains MST routes, day passes, and mobile ticketing. " +
                "If you are commuting to CSUMB or downtown, start with routes 1, 20, and 23.",
            "Monterey Peninsula"
        );

        createIssueIfMissing(
            systemUser,
            guidesCategory,
            "Emergency Kit Checklist for Coastal Weather",
            "Build a 72-hour kit with water, shelf-stable food, medications, batteries, and printed emergency contacts. " +
                "Store one kit at home and one in your car.",
            "Countywide"
        );

        createIssueIfMissing(
            systemUser,
            guidesCategory,
            "New Resident Starter Guide",
            "Just moved here? This post walks through utility setup, library cards, parking permits, and local volunteer groups " +
                "to help you settle in quickly.",
            "Monterey County"
        );
        }

        private void createIssueIfMissing(User user, Category category, String title, String description, String locationText) {
        boolean exists = issueRepository.findByCategory_CategoryId(category.getCategoryId())
            .stream()
            .anyMatch(issue -> issue.getTitle() != null && issue.getTitle().trim().equalsIgnoreCase(title));

        if (exists) {
            return;
        }

        Issue issue = new Issue();
        issue.setUser(user);
        issue.setCategory(category);
        issue.setTitle(title);
        issue.setDescription(description);
        issue.setStatus(DEFAULT_ISSUE_STATUS);
        issue.setLocationText(locationText);
        issueRepository.save(issue);
        }

    private void migrateLegacyCommunityThreadTitle(Category communityCategory) {
        List<Issue> communityIssues = issueRepository.findByCategory_CategoryId(communityCategory.getCategoryId());
        boolean updated = false;

        for (Issue issue : communityIssues) {
            String currentTitle = issue.getTitle();
            if (currentTitle != null && currentTitle.trim().equalsIgnoreCase(LEGACY_COMMUNITY_THREAD_TITLE)) {
                issue.setTitle(EVENT_STYLE_COMMUNITY_TITLE);
                issue.setDescription(EVENT_STYLE_COMMUNITY_DESCRIPTION);
                updated = true;
            }
        }

        if (updated) {
            issueRepository.saveAll(communityIssues);
        }
    }

    private Category findOrCreateCategory(String name) {
        Optional<Category> existing = categoryRepository.findByName(name);
        if (existing.isPresent()) {
            return existing.get();
        }

        Category category = new Category();
        category.setName(name);
        return categoryRepository.save(category);
    }

    private User findOrCreateSystemUser() {
        return userRepository.findByEmailIgnoreCase("system@montereyforum.local")
                .orElseGet(() -> {
                    User user = new User();
                    user.setUsername("system");
                    user.setEmail("system@montereyforum.local");
                    user.setPasswordHash(null);
                    return userRepository.save(user);
                });
    }

    private void ensureDefaultAdminUser() {
        User admin = userRepository.findByEmailIgnoreCase(DEFAULT_ADMIN_EMAIL)
                .orElseGet(User::new);

        admin.setEmail(DEFAULT_ADMIN_EMAIL);
        admin.setUsername(DEFAULT_ADMIN_USERNAME);
        admin.setPasswordHash(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
        userRepository.save(admin);
    }
}
