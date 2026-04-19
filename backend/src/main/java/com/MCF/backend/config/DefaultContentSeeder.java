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
        findOrCreateCategory("Guides");
        findOrCreateCategory("Neighborhood");

        migrateLegacyCommunityThreadTitle(community);

        if (issueRepository.count() > 0) {
            return;
        }

        User systemUser = findOrCreateSystemUser();

        Category announcementsCategory = categoryRepository.findById(announcementsCategoryId).orElse(announcements);

        Issue welcomeAnnouncement = new Issue();
        welcomeAnnouncement.setUser(systemUser);
        welcomeAnnouncement.setCategory(announcementsCategory);
        welcomeAnnouncement.setTitle("Welcome to Monterey Forum");
        welcomeAnnouncement.setDescription("Welcome! Use this space for official updates and community news.");
        welcomeAnnouncement.setStatus("OPEN");
        issueRepository.save(welcomeAnnouncement);

        Issue sampleBulletin = new Issue();
        sampleBulletin.setUser(systemUser);
        sampleBulletin.setCategory(community);
        sampleBulletin.setTitle(EVENT_STYLE_COMMUNITY_TITLE);
        sampleBulletin.setDescription(EVENT_STYLE_COMMUNITY_DESCRIPTION);
        sampleBulletin.setStatus("OPEN");
        issueRepository.save(sampleBulletin);
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
