package com.sky.mapper;

import com.sky.entity.TokenBlacklist;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.Optional;

@Mapper
public interface TokenBlacklistMapper {

    @Insert("insert into token_blacklist (token_hash, token_type, subject_id, expires_at, reason) " +
            "values (#{tokenHash}, #{tokenType}, #{subjectId}, #{expiresAt}, #{reason})")
    void insert(TokenBlacklist tokenBlacklist);

    @Select("select * from token_blacklist where token_hash = #{tokenHash}")
    Optional<TokenBlacklist> selectByTokenHash(String tokenHash);

    int deleteByExpiresAtBefore(LocalDateTime before);
}
