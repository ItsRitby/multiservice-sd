package co.edu.uptc.sd.multiservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "person")
@Getter
@Setter
public class PersonEntity {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(name = "last_name1", nullable = false, length = 100)
    private String lastName1;

    @Column(name = "last_name2", length = 100)
    private String lastName2;
}
