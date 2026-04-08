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
    private UserRepository userRepository; 

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        AppUser user = userRepository.findByEmail(email);

        if (user == null) {
            user = new AppUser();
            user.setEmail(email);
            user.setFullName(name);
            user.setProfilePhotoUrl(picture); //this takes their google account's pfp
            user.setRole("ROLE_USER"); //this gives students standard access
            userRepository.save(user);
        } else if (user.getProfilePhotoUrl() == null || user.getProfilePhotoUrl().isEmpty()) {
            user.setProfilePhotoUrl(picture);
            userRepository.save(user);
        }

        // this tells spring security what the user's role is.
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole()));

        // this returns an autheticated user.
        return new DefaultOAuth2User(authorities, oAuth2User.getAttributes(), "email");
    }
}
