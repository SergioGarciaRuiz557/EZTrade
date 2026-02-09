package com.trading.platform.eztrade.user.adapter.out.persistence.jpa;

import com.trading.platform.eztrade.user.domain.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Adaptador de persistencia para la entidad de dominio {@link User}.
 * <p>
 * Implementa el puerto de salida de repositorio de usuarios y delega
 * las operaciones de acceso a datos en el repositorio JPA subyacente.
 */
@Repository
public class UserRepository implements com.trading.platform.eztrade.user.application.ports.out.UserRepository {

    private final JpaUserRepository jpaUserRepository;

    /**
     * Crea una nueva instancia del adaptador de repositorio de usuarios.
     *
     * @param jpaUserRepository repositorio JPA que realiza las operaciones de base de datos
     */
    public UserRepository(JpaUserRepository jpaUserRepository) {
        this.jpaUserRepository = jpaUserRepository;
    }

    /**
     * Busca un usuario por su correo electrónico.
     * <p>
     * Delegado al método correspondiente del {@link JpaUserRepository}.
     *
     * @param username correo electrónico del usuario a buscar
     * @return un {@link Optional} que contiene el usuario si existe, vacío en caso contrario
     */
    @Override
    public Optional<User> findByEmail(String username) {
        return jpaUserRepository.findByEmail(username);
    }

    /**
     * Persiste un usuario en la base de datos.
     * <p>
     * Si el usuario ya existe, se actualizan sus datos; en caso contrario, se crea un nuevo registro.
     *
     * @param user entidad de usuario a guardar
     * @return la entidad {@link User} guardada, incluida la información generada por la base de datos
     */
    @Override
    public User save(User user) {
        return jpaUserRepository.save(user);
    }
}

