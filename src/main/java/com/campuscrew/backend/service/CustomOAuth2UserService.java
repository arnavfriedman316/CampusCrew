package com.campuscrew.backend.service; // Change this to match your folder structure!

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired; // Update if your repo has a different name
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.campuscrew.backend.entity.AppUser;
import com.campuscrew.backend.repository.UserRepository;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository; // Assuming your repository is named UserRepository

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. Let Spring Security do the heavy lifting of talking to Google
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 2. Extract the user's details from Google's response
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        // 3. Check if this person is already in our Neon Database
        AppUser user = userRepository.findByEmail(email);

        if (user == null) {
            // BRAND NEW USER! Let's register them automatically.
            user = new AppUser();
            user.setEmail(email);
            user.setFullName(name);
            user.setProfilePhotoUrl(picture); // Snag their Google Profile Picture!
            user.setRole("ROLE_USER"); // Give them standard student access
            userRepository.save(user);
        } else if (user.getProfilePhotoUrl() == null || user.getProfilePhotoUrl().isEmpty()) {
            // Optional: If they already exist but don't have a photo, give them their Google one
            user.setProfilePhotoUrl(picture);
            userRepository.save(user);
        }

        // 4. Tell Spring Security what their CampusCrew Role is
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole()));

        // 5. Return the authenticated user (using their email as their primary ID)
        return new DefaultOAuth2User(authorities, oAuth2User.getAttributes(), "email");
    }
}
