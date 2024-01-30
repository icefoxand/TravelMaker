package com.ssafy.gumibom.global.config;

import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordEncodeConfig implements PasswordEncoder {
    @Override
    public String encode(CharSequence rawPassword) {
        return rawPassword.toString();
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return encodedPassword.equals(encode(rawPassword));
    }
}
