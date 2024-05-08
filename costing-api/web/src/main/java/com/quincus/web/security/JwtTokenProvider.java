package com.quincus.web.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private static final String AUTH = "auth";
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";

    private static final long VALIDITY_IN_MILLISECONDS = 3600000;

    @Autowired
    private PassThroughUserDetailsService userDetailsService;

    private SecretKey secretKey;

    @PostConstruct
    protected void init() {
        secretKey = Keys.hmacShaKeyFor(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(AuthUser user) {

        final Claims claims = Jwts.claims().setSubject(user.getUsername());
        claims.put(AUTH, user.getAuthorities());

        final Date now = new Date();
        final Date validity = new Date(now.getTime() + VALIDITY_IN_MILLISECONDS);

        return Jwts.builder()//
                .setId(UUID.randomUUID().toString())
                .setClaims(claims)//
                .setIssuedAt(now)//
                .setExpiration(validity)//
                .signWith(secretKey, SignatureAlgorithm.HS256)//
                .compact();
    }

    private List<SimpleGrantedAuthority> createAuth(List<String> auths) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        auths.forEach(e -> authorities.add(new SimpleGrantedAuthority(e)));
        return authorities;
    }

    public Authentication getAuthentication(String token) {
        final UserDetails userDetails = userDetailsService.loadUserFromAuth(getUsername(token), getAuthorities(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String getUsername(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().getSubject();
    }

    public List<SimpleGrantedAuthority> getAuthorities(String token) {
        List<String> auths = (ArrayList) Jwts.parserBuilder()
                .setSigningKey(secretKey).build()
                .parseClaimsJws(token).getBody().get(AUTH);
        return createAuth(auths);
    }

    public String resolveToken(HttpServletRequest req) {
        String bearerToken = req.getHeader(AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith(BEARER)) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            throw new IllegalStateException("Expired or invalid JWT token");
        }
    }

}
