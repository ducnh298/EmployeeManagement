package ducnh.springboot.model.entity;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "user")
@JsonFormat(pattern = "yyyy-MM-dd")
@NoArgsConstructor
@Getter
@Setter

public class UserEntity extends BaseEntity {
	@JsonIgnore
	@Column
	private String username;

	@JsonIgnore
	@Column
	private String password;

	@Column
	private String fullname;

	@Column
	private String checkinCode;

	@Column
	private Date dateOfBirth;

	@Column
	private String email;

	
	@OneToMany(mappedBy = "user", targetEntity = CheckinEntity.class, cascade = CascadeType.ALL,fetch = FetchType.LAZY)
	private List<CheckinEntity> checkins = new ArrayList<CheckinEntity>();

	@ManyToMany(fetch = FetchType.EAGER)
	@BatchSize(size=5)
	@JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<RoleEntity> roles = new HashSet<RoleEntity>();

	
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name="workinghour_id",referencedColumnName = "id")
	@Fetch(FetchMode.SELECT)
	@BatchSize(size=10)
	@JsonIgnore
	private WorkingHourEntity workinghour;
	
}
