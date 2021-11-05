package org.acme.fintech.controller;

//@RestController
//public class GreetingController {
//
//    @Autowired
//    private EmployeeRepository employeeRepository;
//
//    @GetMapping("/test")
//    public ResponseEntity<Employee> greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
//
//        Employee employee = new Employee();
//        employee.setName(UUID.randomUUID().toString());
//
//        Address address = new Address();
//        address.setAddressLine1("line 1");
//
//        // set child reference
//        address.setEmployee(employee);
//
//        // set parent reference
//        employee.setAddress(address);
//
//        // save the parent
//        employee = employeeRepository.save(employee);
//
//        return ResponseEntity.ok(employee);
//    }
//}
