package com.cinesmart;

import com.cinesmart.user.entity.User;
import com.cinesmart.user.entity.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Entity Tests")
class UserTest {

    @Test
    @DisplayName("Should create user with default ROLE_CUSTOMER and active status")
    void testUserCreationDefaults() {
        User user = new User("customer@test.com", "hashed_pass", "John Doe", "1234567890", null);

        assertEquals("customer@test.com", user.getEmail());
        assertEquals("John Doe", user.getFullName());
        assertEquals(UserRole.ROLE_CUSTOMER, user.getRole());
        assertTrue(user.isActive());
    }

    @Test
    @DisplayName("Should correctly support ADMIN and STAFF roles")
    void testUserRoles() {
        User admin = new User("admin@test.com", "hashed_pass", "Admin User", null, UserRole.ROLE_ADMIN);
        User staff = new User("staff@test.com", "hashed_pass", "Staff User", null, UserRole.ROLE_STAFF);

        assertEquals(UserRole.ROLE_ADMIN, admin.getRole());
        assertEquals(UserRole.ROLE_STAFF, staff.getRole());
    }
}
