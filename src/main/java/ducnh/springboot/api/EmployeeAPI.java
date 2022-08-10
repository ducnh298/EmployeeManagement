package ducnh.springboot.api;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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
import ducnh.springboot.projection.UserSlim;
import ducnh.springboot.repository.WorkingHourRepository;
import ducnh.springboot.service.IMailService;
import ducnh.springboot.service.IUserService;
import ducnh.springboot.service.IWorkingHourService;

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
	@Secured({ "HR", "STAFF" })
	public List<UserSlim> getEmployees() {
		return userService.findAll(UserSlim.class);
	}

	@GetMapping("/find-all/{orderBy}")
	@Secured({ "HR", "STAFF" })
	public List<UserSlim> getEmployeesOrderByFullname(@PathVariable String orderBy) {
		if (orderBy.equalsIgnoreCase("ASC"))
			return userService.findAllOrderByFullnameASC(UserSlim.class);
		else if (orderBy.equalsIgnoreCase("DESC"))
			return userService.findAllOrderByFullnameDESC(UserSlim.class);
		return null;
	}

	@GetMapping("/find-by-name")
	@Secured({ "HR", "STAFF" })
	public List<UserSlim> findEmployeesByName(@RequestBody String key) {
		return userService.findByFullnameIgnoreCaseContaining(UserSlim.class, key);
	}

	@GetMapping("/find-by-user-name")
	@Secured({ "HR", "STAFF" })
	public UserSlim findEmployeeByUsername(@RequestBody String username) {
		return userService.findByUsername(UserSlim.class, username);
	}

	@GetMapping("/find-all-having-role")
	@Secured({ "HR", "STAFF" })
	public List<UserDTO> findEmployeeHavingRole(@RequestBody Map<String, String> json) {
		return userService.findAllEmployeeHavingRole(json.get("roleName"));
	}

	@GetMapping("/find-workinghour_by_userid/{id}")
	@Secured({ "HR", "STAFF" })
	public WorkingHourDTO findWorkingHourByUserId(@PathVariable("id") Long id) {
		UserDTO user = userService.findById(UserDTO.class, id);
		return user.getWorkingHour();
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
