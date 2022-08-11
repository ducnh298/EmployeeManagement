package ducnh.springboot.api;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ducnh.springboot.dto.UserDTO;
import ducnh.springboot.dto.WorkingHourDTO;
import ducnh.springboot.model.entity.UserEntity;
import ducnh.springboot.projection.UserSlim;
import ducnh.springboot.projection.UserSlimClass;
import ducnh.springboot.repository.WorkingHourRepository;
import ducnh.springboot.service.IMailService;
import ducnh.springboot.service.IUserService;
import ducnh.springboot.service.IWorkingHourService;
import ducnh.springboot.specifications.UserSpecification;

@RestController
@RequestMapping("/employee-management")
public class EmployeeAPI {

	@Autowired
	IUserService userService;

	@Autowired
	IWorkingHourService workingHourService;

	@Autowired
	IMailService mailService;

	@Autowired
	WorkingHourRepository workingHourRepository;

	@GetMapping(value = "/admin")
	@Secured({ "HR" })
	public String adminPage() {
		return "welcome admin";
	}

	@GetMapping("/find-all")
	public List<UserSlim> getEmployees() {
		return userService.findAll(UserSlim.class);
	}

	@GetMapping("/find-all/{orderBy}")
	public List<UserDTO> getEmployeesOrderByFullname(@PathVariable String orderBy) {
		if (orderBy.equalsIgnoreCase("ASC"))
			return userService.findAllOrderByFullnameASC(UserDTO.class);
		else if (orderBy.equalsIgnoreCase("DESC"))
			return userService.findAllOrderByFullnameDESC(UserDTO.class);
		return null;
	}

	@GetMapping("/find-by-name")
	public List<UserSlimClass> findEmployeesByName(@RequestBody String key) {
		return userService.findByFullnameIgnoreCaseContaining(UserSlimClass.class, key);
	}

	@GetMapping("/find-by-user-name")
	public UserSlim findEmployeeByUsername(@RequestBody String username) {
		return userService.findByUsername(UserSlim.class, username);
	}

	@GetMapping("/find-all-having-role")
	public List<UserDTO> findEmployeeHavingRole(@RequestBody Map<String, String> json) {
		return userService.findAllEmployeeHavingRole(json.get("roleName"));
	}

	@GetMapping("/find-workinghour_by_userid/{id}")
	public WorkingHourDTO findWorkingHourByUserId(@PathVariable("id") Long id) {
		UserDTO user = userService.findById(UserDTO.class, id);
		return user.getWorkingHour();
	}

	@GetMapping("/find-all-having-fullname-like-and-role")
	public List<UserDTO> findEmployeeHavingFullnameLikeAndRole(@RequestBody Map<String, Object> json) {
		Specification<UserEntity> spec = UserSpecification.hasFullNameLike(json.get("name").toString())
				.and(UserSpecification.hasRole(json.get("roleName").toString())
						.and(UserSpecification.hasAgeDiff((Boolean)json.get("greater"), Integer.parseInt(json.get("age").toString()))));

		return userService.findAllHavingSpecifications(spec);
	}

	@PostMapping
	@Secured("HR")
	public UserDTO createUser(@RequestBody UserDTO user) {
		user = userService.save(user);
		if (user != null) {
			StringBuilder content = new StringBuilder("");
			content.append("<h1>Hi ");
			content.append(user.getFullname());
			content.append("!</h1><br><h2>Your NCC's account has been created by: ");
			content.append(user.getCreatedBy());
			content.append(" --- at: ");
			content.append(user.getCreatedDate());
			content.append("<br><br>Your username: ");
			content.append(user.getUsername());
			content.append("<br>Your default password: 12345</h2>");
			System.out.println(
					mailService.sendMail(user.getEmail(), " NCC's Employee Account Created ", content.toString()));
		}
		return user;
	}

	@PutMapping("/{id}")
	@Secured("HR")
	public UserDTO updateEmployee(@RequestBody UserDTO user, @PathVariable Long id) {
		user.setId(id);
		return userService.save(user);
	}

	@DeleteMapping
	@Secured("HR")
	public String deleteEmployees(@RequestBody Long[] ids) {
		userService.delete(ids);
		return "deleted " + ids.length;
	}

	@PostMapping("/{id}/set-working-hour")
	@Secured("HR")
	public WorkingHourDTO setWorkingHour(@PathVariable("id") Long id, @RequestBody WorkingHourDTO workingHour) {
		UserDTO user = new UserDTO();
		user.setId(id);
		workingHour.setUser(user);
		return workingHourService.save(workingHour);
	}

}
