import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

// Singleton Pattern for Admin Control Panel
class AdminControlPanel extends JFrame {
    private static AdminControlPanel instance = null;  // Singleton instance
    private JTree userTree;  // Tree to display users and groups
    private DefaultTreeModel treeModel;  // Model for the tree
    private JTextField userNameTextField;  // TextField to input user names
    private JTextField groupNameTextField;  // TextField to input group names
    
    private AdminControlPanel() {
        setTitle("Admin Control Panel");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Initialize the root of the tree
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
        treeModel = new DefaultTreeModel(root);
        userTree = new JTree(treeModel);
        JScrollPane treeScrollPane = new JScrollPane(userTree);
        add(treeScrollPane, BorderLayout.CENTER);
        
        // Panel for user and group creation controls
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(8, 2));
        
        // User creation controls
        controlPanel.add(new JLabel("User Name:"));
        userNameTextField = new JTextField();
        controlPanel.add(userNameTextField);
        
        JButton createUserButton = new JButton("Create User");
        createUserButton.addActionListener(e -> createUser());
        controlPanel.add(createUserButton);
        
        // Group creation controls
        controlPanel.add(new JLabel("Group Name:"));
        groupNameTextField = new JTextField();
        controlPanel.add(groupNameTextField);
        
        JButton createGroupButton = new JButton("Create Group");
        createGroupButton.addActionListener(e -> createGroup());
        controlPanel.add(createGroupButton);
        
        // Open user view button
        JButton openUserViewButton = new JButton("Open User View");
        openUserViewButton.addActionListener(e -> openUserView());
        controlPanel.add(openUserViewButton);
        
        // Validate IDs button
        JButton validateIDsButton = new JButton("Validate IDs");
        validateIDsButton.addActionListener(e -> validateIDs());
        controlPanel.add(validateIDsButton);
        
        // Last updated user button
        JButton lastUpdatedUserButton = new JButton("Last Updated User");
        lastUpdatedUserButton.addActionListener(e -> showLastUpdatedUser());
        controlPanel.add(lastUpdatedUserButton);
        
        add(controlPanel, BorderLayout.EAST);
        
        // Panel for analysis buttons
        JPanel analysisPanel = new JPanel();
        analysisPanel.setLayout(new GridLayout(4, 1));
        
        // Analysis buttons
        JButton totalUsersButton = new JButton("Total Users");
        totalUsersButton.addActionListener(e -> showAnalysis("Total Users"));
        analysisPanel.add(totalUsersButton);
        
        JButton totalGroupsButton = new JButton("Total Groups");
        totalGroupsButton.addActionListener(e -> showAnalysis("Total Groups"));
        analysisPanel.add(totalGroupsButton);
        
        JButton totalTweetsButton = new JButton("Total Tweets");
        totalTweetsButton.addActionListener(e -> showAnalysis("Total Tweets"));
        analysisPanel.add(totalTweetsButton);
        
        JButton positiveTweetsButton = new JButton("Positive Tweets %");
        positiveTweetsButton.addActionListener(e -> showAnalysis("Positive Tweets %"));
        analysisPanel.add(positiveTweetsButton);
        
        add(analysisPanel, BorderLayout.SOUTH);
    }
    
    // Method to get the singleton instance of Admin Control Panel
    public static AdminControlPanel getInstance() {
        if (instance == null) {
            instance = new AdminControlPanel();
        }
        return instance;
    }
    
    // Getter for the tree model
    public DefaultTreeModel getTreeModel() {
        return treeModel;
    }
    
    // Method to create a new user and add it to the tree
    private void createUser() {
        String userName = userNameTextField.getText();
        if (!userName.isEmpty()) {
            User newUser = new User(userName);
            DefaultMutableTreeNode userNode = new DefaultMutableTreeNode(newUser);
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) userTree.getLastSelectedPathComponent();
            if (selectedNode == null) {
                selectedNode = (DefaultMutableTreeNode) treeModel.getRoot();
            }
            selectedNode.add(userNode);
            treeModel.reload();
        }
    }
    
    // Method to create a new group and add it to the tree
    private void createGroup() {
        String groupName = groupNameTextField.getText();
        if (!groupName.isEmpty()) {
            UserGroup newGroup = new UserGroup(groupName);
            DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(newGroup);
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) userTree.getLastSelectedPathComponent();
            if (selectedNode == null) {
                selectedNode = (DefaultMutableTreeNode) treeModel.getRoot();
            }
            selectedNode.add(groupNode);
            treeModel.reload();
        }
    }
    
    // Method to open the user view for the selected user
    private void openUserView() {
        TreePath selectedPath = userTree.getSelectionPath();
        if (selectedPath != null) {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
            if (selectedNode.getUserObject() instanceof User) {
                User selectedUser = (User) selectedNode.getUserObject();
                new UserView(selectedUser);
            }
        }
    }
    
    // Method to show analysis results
    private void showAnalysis(String analysisType) {
        Visitor visitor = new TweetVisitor();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        visitNodes(root, visitor);
        JOptionPane.showMessageDialog(this, visitor.getResult(analysisType));
    }
    
    // Helper method to visit nodes in the tree
    private void visitNodes(DefaultMutableTreeNode node, Visitor visitor) {
        Object userObject = node.getUserObject();
        if (userObject instanceof User) {
            visitor.visitUser((User) userObject);
        } else if (userObject instanceof UserGroup) {
            visitor.visitUserGroup((UserGroup) userObject);
        }
        
        Enumeration<?> children = node.children();
        while (children.hasMoreElements()) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) children.nextElement();
            visitNodes(childNode, visitor);
        }
    }
    
    // Method to validate user and group IDs
    private void validateIDs() {
        Set<String> ids = new HashSet<>();
        boolean allValid = true;
        StringBuilder errors = new StringBuilder();

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        Enumeration<?> enumeration = root.breadthFirstEnumeration();
        while (enumeration.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) enumeration.nextElement();
            Object userObject = node.getUserObject();
            String id = null;
            if (userObject instanceof User) {
                id = ((User) userObject).getId();
            } else if (userObject instanceof UserGroup) {
                id = ((UserGroup) userObject).getId();
            }
            if (id != null) {
                if (id.contains(" ")) {
                    allValid = false;
                    errors.append("ID contains spaces: ").append(id).append("\n");
                }
                if (!ids.add(id)) {
                    allValid = false;
                    errors.append("Duplicate ID found: ").append(id).append("\n");
                }
            }
        }

        if (allValid) {
            JOptionPane.showMessageDialog(this, "All IDs are valid.");
        } else {
            JOptionPane.showMessageDialog(this, "ID validation failed:\n" + errors.toString());
        }
    }
    
    // Method to show the ID of the last updated user
    private void showLastUpdatedUser() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        Enumeration<?> enumeration = root.breadthFirstEnumeration();
        User lastUpdatedUser = null;
        long lastUpdateTime = 0;

        while (enumeration.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) enumeration.nextElement();
            if (node.getUserObject() instanceof User) {
                User user = (User) node.getUserObject();
                if (user.getLastUpdateTime() > lastUpdateTime) {
                    lastUpdateTime = user.getLastUpdateTime();
                    lastUpdatedUser = user;
                }
            }
        }

        if (lastUpdatedUser != null) {
            JOptionPane.showMessageDialog(this, "Last updated user: " + lastUpdatedUser.getId());
        } else {
            JOptionPane.showMessageDialog(this, "No user updates found.");
        }
    }
}

// Composite and Observer Pattern for Users
class User extends Observable implements Observer {
    private String id;  // User ID
    private List<User> followers;  // List of followers
    private List<User> followings;  // List of followings
    private List<String> newsFeed;  // News feed list
    private long creationTime;  // Creation time
    private long lastUpdateTime;  // Last update time
    
    public User(String id) {
        this.id = id;
        this.followers = new ArrayList<>();
        this.followings = new ArrayList<>();
        this.newsFeed = new ArrayList<>();
        this.creationTime = System.currentTimeMillis();
        this.lastUpdateTime = creationTime;
    }
    
    public String getId() {
        return id;
    }
    
    public long getCreationTime() {
        return creationTime;
    }
    
    public long getLastUpdateTime() {
        return lastUpdateTime;
    }
    
    // Method to follow another user
    public void follow(User user) {
        if (!followings.contains(user)) {
            followings.add(user);
            user.addFollower(this);
            user.addObserver(this);  // Observe the followed user
        }
    }
    
    // Helper method to add a follower
    private void addFollower(User user) {
        if (!followers.contains(user)) {
            followers.add(user);
        }
    }
    
    // Method to post a tweet
    public void postTweet(String message) {
        newsFeed.add(id + ": " + message);  // Add the user ID to the tweet
        setChanged();
        notifyObservers(id + ": " + message);  // Notify observers with the user ID and message
        updateLastUpdateTime();
    }
    
    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof String) {
            newsFeed.add((String) arg);
            updateLastUpdateTime();
        }
    }
    
    public List<String> getNewsFeed() {
        return newsFeed;
    }

    public List<User> getFollowings() {
        return followings;
    }
    
    private void updateLastUpdateTime() {
        lastUpdateTime = System.currentTimeMillis();
    }
}

// Composite Pattern for User Groups
class UserGroup {
    private String id;  // Group ID
    private List<Object> members;  // List of members (users and groups)
    private long creationTime;  // Creation time
    
    public UserGroup(String id) {
        this.id = id;
        this.members = new ArrayList<>();
        this.creationTime = System.currentTimeMillis();
    }
    
    public String getId() {
        return id;
    }
    
    public long getCreationTime() {
        return creationTime;
    }
    
    // Method to add a member to the group
    public void addMember(Object member) {
        members.add(member);
    }
    
    public List<Object> getMembers() {
        return members;
    }
    
    // Method to accept a visitor
    public void accept(Visitor visitor) {
        visitor.visitUserGroup(this);
        for (Object member : members) {
            if (member instanceof User) {
                visitor.visitUser((User) member);
            } else if (member instanceof UserGroup) {
                ((UserGroup) member).accept(visitor);
            }
        }
    }
}

// Visitor Pattern for Analysis
interface Visitor {
    void visitUser(User user);
    void visitUserGroup(UserGroup userGroup);
    String getResult(String analysisType);
}

class TweetVisitor implements Visitor {
    private int userCount;  // Counter for users
    private int groupCount;  // Counter for groups
    private int tweetCount;  // Counter for tweets
    private int positiveTweetCount;  // Counter for positive tweets
    private static final List<String> POSITIVE_WORDS = Arrays.asList("great", "excellent", "nice", "good", "best");
    
    public TweetVisitor() {
        userCount = 0;
        groupCount = 0;
        tweetCount = 0;
        positiveTweetCount = 0;
    }
    
    @Override
    public void visitUser(User user) {
        userCount++;
        for (String tweet : user.getNewsFeed()) {
            tweetCount++;
            if (isPositive(tweet)) {
                positiveTweetCount++;
            }
        }
    }
    
    @Override
    public void visitUserGroup(UserGroup userGroup) {
        groupCount++;
    }
    
    // Helper method to check if a tweet is positive
    private boolean isPositive(String tweet) {
        for (String word : POSITIVE_WORDS) {
            if (tweet.toLowerCase().contains(word)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public String getResult(String analysisType) {
        switch (analysisType) {
            case "Total Users":
                return "Total Users: " + userCount;
            case "Total Groups":
                return "Total Groups: " + groupCount;
            case "Total Tweets":
                return "Total Tweets: " + tweetCount;
            case "Positive Tweets %":
                return "Positive Tweets %: " + (tweetCount == 0 ? 0 : (positiveTweetCount * 100 / tweetCount)) + "%";
            default:
                return "Invalid Analysis Type";
        }
    }
}

// GUI for User View
class UserView extends JFrame {
    private User user;  // The user associated with this view
    private JList<String> followingsList;  // List to display followings
    private DefaultListModel<String> followingsListModel;  // Model for the followings list
    private JList<String> newsFeedList;  // List to display news feed
    private DefaultListModel<String> newsFeedListModel;  // Model for the news feed list
    
    public UserView(User user) {
        this.user = user;
        
        setTitle("User View: " + user.getId());
        setSize(600, 400);
        setLayout(new BorderLayout());
        
        // Follow user controls
        JPanel followPanel = new JPanel(new BorderLayout());
        JTextArea followUserTextField = new JTextArea();
        followPanel.add(new JLabel("Follow User ID:"), BorderLayout.NORTH);
        followPanel.add(followUserTextField, BorderLayout.CENTER);
        
        JButton followUserButton = new JButton("Follow User");
        followUserButton.addActionListener(e -> followUser(followUserTextField.getText()));
        followPanel.add(followUserButton, BorderLayout.EAST);
        
        // List to display followings
        followingsListModel = new DefaultListModel<>();
        followingsList = new JList<>(followingsListModel);
        JScrollPane followingsScrollPane = new JScrollPane(followingsList);
        
        // Post tweet controls
        JPanel tweetPanel = new JPanel(new BorderLayout());
        JTextArea tweetTextField = new JTextArea();
        tweetPanel.add(new JLabel("Post Tweet:"), BorderLayout.NORTH);
        tweetPanel.add(tweetTextField, BorderLayout.CENTER);
        
        JButton postTweetButton = new JButton("Post Tweet");
        postTweetButton.addActionListener(e -> postTweet(tweetTextField.getText()));
        tweetPanel.add(postTweetButton, BorderLayout.EAST);
        
        // List to display news feed
        newsFeedListModel = new DefaultListModel<>();
        newsFeedList = new JList<>(newsFeedListModel);
        JScrollPane newsFeedScrollPane = new JScrollPane(newsFeedList);
        
        // Layout for followings and news feed
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, followingsScrollPane, newsFeedScrollPane);
        
        add(followPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(tweetPanel, BorderLayout.SOUTH);
        
        setVisible(true);
        
        refreshFollowingsList();
        refreshNewsFeedList();
        
        System.out.println("User creation time: " + user.getCreationTime());
        user.addObserver((o, arg) -> refreshNewsFeedList());  // Add observer to refresh news feed
    }
    
    // Method to follow another user by ID
    private void followUser(String userId) {
        User targetUser = findUserById(userId);
        if (targetUser != null) {
            user.follow(targetUser);
            refreshFollowingsList();
        } else {
            JOptionPane.showMessageDialog(this, "User not found");
        }
    }

    // Method to find a user by ID in the tree
    private User findUserById(String userId) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) AdminControlPanel.getInstance().getTreeModel().getRoot();
        Enumeration<?> enumeration = root.breadthFirstEnumeration();
        while (enumeration.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) enumeration.nextElement();
            if (node.getUserObject() instanceof User) {
                User user = (User) node.getUserObject();
                if (user.getId().equals(userId)) {
                    return user;
                }
            }
        }
        return null;
    }
    
    // Method to post a tweet
    private void postTweet(String message) {
        user.postTweet(message);
        refreshNewsFeedList();
    }
    
    // Method to refresh the followings list
    private void refreshFollowingsList() {
        followingsListModel.clear();
        for (User following : user.getFollowings()) {
            followingsListModel.addElement(following.getId());
        }
    }
    
    // Method to refresh the news feed list
    private void refreshNewsFeedList() {
        newsFeedListModel.clear();
        for (String tweet : user.getNewsFeed()) {
            newsFeedListModel.addElement(tweet);
        }
        System.out.println("User last update time: " + user.getLastUpdateTime());
    }
}

// Main Application Entry
public class Project2 {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AdminControlPanel adminControlPanel = AdminControlPanel.getInstance();
            adminControlPanel.setVisible(true);
        });
    }
}
