package hr.example.employee;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public Employee createEmployee(String firstName, String lastName, String email,
                                    String department, String position, LocalDate hireDate, Double salary) {
        Employee employee = new Employee(firstName, lastName, email);
        employee.setDepartment(department);
        employee.setPosition(position);
        employee.setHireDate(hireDate);
        employee.setSalary(salary);
        return employeeRepository.save(employee);
    }

    @Transactional(readOnly = true)
    public Page<Employee> list(Pageable pageable) {
        return employeeRepository.findAll(pageable);
    }
}

