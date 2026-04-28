package edu.eci.dosw.mapper;

import edu.eci.dosw.entity.RefreshTokenEntity;
import edu.eci.dosw.model.RefreshToken;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenMapper {

    private final AccountMapper accountMapper;

    public RefreshTokenMapper(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    /** RefreshTokenEntity → RefreshToken (modelo de dominio) */
    public RefreshToken toModel(RefreshTokenEntity entity) {
        if (entity == null) return null;
        return new RefreshToken(
                entity.getId(),
                entity.getToken(),
                accountMapper.toModel(entity.getAccount()),
                entity.isRevoked()
        );
    }

    /** RefreshToken (modelo de dominio) → RefreshTokenEntity */
    public RefreshTokenEntity toEntity(RefreshToken model) {
        if (model == null) return null;
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setId(model.getId());
        entity.setToken(model.getToken());
        entity.setRevoked(model.isRevoked());
        if (model.getAccount() != null) {
            entity.setAccount(accountMapper.toEntity(model.getAccount()));
        }
        return entity;
    }
}
