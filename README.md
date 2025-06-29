🛠️ How to Use Quarkus Query Sculptor
If you're familiar with Spring Data JPA Specification, you'll find this library very familiar.

This library provides a similar way to construct type-safe, composable, and dynamic queries in Quarkus, especially for PanacheRepository or similar data access patterns.

🔰 Example Usage
```
public class UserRepository implements QuerySculptorExecutor<User> {
    @Override
    public Class<User> entityClass() {
        return User.class;
    }
}

```
```
public class UserQuerySculptor {
    public static QuerySculptor<User> hasStatus(String status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static QuerySculptor<User> withUsernameLike(String username) {
        return (root, query, cb) -> cb.like(root.get("username"), "%" + username + "%");
    }
}
```
```
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public User similarUsername(String username) {
        return userRepository.findOne(UserQuerySculptor.withUsernameLike(username));
    }
}
```
