package me.niallmurray.slipstreamf1.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "driver")
public class Driver implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column()
  private Integer carNumber;
  @Column()
  private String shortName;
  @Column()
  private String wikiPage;
  @Column()
  private String firstName;
  @Column()
  private String surname;
  @Column()
  private String dateOfBirth;
  @Column()
  private String nationality;
  @Column()
  private Double points;
  @Column()
  private Integer standing;
  @Column()
  private String constructor;
  @ManyToMany
  private List<Team> teams = new ArrayList<>();

  @Override
  public String toString() {
    return surname;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Driver driver = (Driver) o;
    return Objects.equals(id, driver.id) && Objects.equals(shortName, driver.shortName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, shortName);
  }
}
