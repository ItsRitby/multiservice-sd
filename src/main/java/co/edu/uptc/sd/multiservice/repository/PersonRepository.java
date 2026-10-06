package co.edu.uptc.sd.multiservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.uptc.sd.multiservice.dto.PersonDTO;
import co.edu.uptc.sd.multiservice.entity.PersonEntity;

public interface PersonRepository extends JpaRepository<PersonEntity, Integer> {

    @Query("""
            SELECT new co.edu.uptc.sd.multiservice.dto.PersonDTO(
                        p.id, p.firstName, p.middleName, p.lastName1, p.lastName2)
            FROM PersonEntity p
            WHERE p.id BETWEEN :from AND :to
            """)
    List<PersonDTO> findPageByIdRange(@Param("from") int from, @Param("to") int to);

    @Query("SELECT MAX(p.id) FROM PersonEntity p")
    Optional<Integer> findMaxId();
}