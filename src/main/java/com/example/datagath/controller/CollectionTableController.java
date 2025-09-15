package com.example.datagath.controller;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.example.datagath.dto.ColumnDTO;
import com.example.datagath.dto.TableCreationForm;
import com.example.datagath.dto.TableCreationResponse;
import com.example.datagath.model.CollectionTable;
import com.example.datagath.model.User;
import com.example.datagath.repository.CollectionTableRepository;
import com.example.datagath.service.DynamicTableService;
import com.example.datagath.service.UserService;
import com.itextpdf.text.pdf.PdfStructTreeController.returnType;

import jakarta.servlet.http.HttpServletRequest;

import java.net.http.HttpHeaders;
import java.util.Arrays;

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequestMapping("/tables")
public class CollectionTableController {

    private final CollectionTableRepository collectionTableRepository;
    private DynamicTableService dynamicTableService;
    private UserService userService;

    public CollectionTableController(DynamicTableService dynamicTableService,
            UserService userService, CollectionTableRepository collectionTableRepository) {
        this.dynamicTableService = dynamicTableService;
        this.userService = userService;
        this.collectionTableRepository = collectionTableRepository;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createNewTable(@CookieValue(value = "sessionToken", required = false) String token,
            @RequestBody TableCreationForm tableCreationForm) {

        System.out.println(tableCreationForm.getTableName());
        Map<String, String> columns = tableCreationForm.getColumns().stream()
                .collect(Collectors.toMap(ColumnDTO::getName, ColumnDTO::getType));
        System.out.println(tableCreationForm.getTableName());
        User user = token != null ? userService.validateSessionToken(token) : null;
        if (user != null) {
            TableCreationResponse response = dynamicTableService.createTable(tableCreationForm.getTableName(),
                    user.getId(),
                    columns);
            if (response.getSuccess()) {
                return ResponseEntity
                        .ok(dynamicTableService.findTable(user.getId(), tableCreationForm.getTableName()).getId());
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.getBody());
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No Credentials Provided");
        }
    }

    @GetMapping("/create")
    public String initiateTableCreation(@CookieValue(value = "sessionToken", required = false) String token) {
        User user = token != null ? userService.validateSessionToken(token) : null;
        if (user != null) {
            return "tableCreator";
        } else {
            return "redirect:/login";
        }
    }

    @RequestMapping(value = "/chart", method = { RequestMethod.GET, RequestMethod.POST })
    public ResponseEntity<?> getMethodName(@CookieValue(value = "sessionToken", required = false) String token,
            @RequestParam(required = false) String tableName) {
        User user = token != null ? userService.validateSessionToken(token) : null;
        if (user != null && tableName != null) {
            String echartsFramework = dynamicTableService.formatTableIntoEcharts(tableName, user.getId());
            if (echartsFramework != null) {
                return ResponseEntity.status(HttpStatus.OK).body(echartsFramework);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Issue Fetching Table");
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Insufficient data provided or wrong table");
    };

    @RequestMapping(value = "/input", method = { RequestMethod.GET, RequestMethod.POST })
    public ResponseEntity<?> inputValueIntoTable(
            @RequestParam(required = false) String tableName,
            @RequestParam(required = false) String userToken,
            @RequestParam(required = false) Map<String, String> allParams) {
        if (allParams != null) {
            allParams.remove("tableName");
            allParams.remove("userToken");
        }
        if (allParams == null || allParams.isEmpty()) {
            return ResponseEntity.badRequest().body("No values provided to insert");
        }
        User user = userToken != null ? userService.validateSessionToken(userToken) : null;
        if (user != null) {
            int success = dynamicTableService.insertValueIntoTable(tableName, user.getId(), allParams);
            if (success == 1) {
                return ResponseEntity.status(HttpStatus.ACCEPTED).body("Inserted Successfully");
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("No user token was provided");
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error inserting value");
    }

    @GetMapping("/input/{id}")
    public ResponseEntity<?> getRequestInputByID(@PathVariable("id") String id,
            @RequestParam(required = false) Map<String, String> allParams) {
        CollectionTable table = dynamicTableService.findTable(id);
        if (table != null) {
            int success = dynamicTableService.insertValueIntoTable(table.getName(), table.getOwnerId(), allParams);
            if (success == 1) {
                return ResponseEntity.ok("Inserted Successfully");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error inserting value");
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Table does not exist");
    }

    @GetMapping("/usertables")
    public ResponseEntity<?> getUserTables(@CookieValue(value = "sessionToken", required = false) String token) {
        User user = token != null ? userService.validateSessionToken(token) : null;
        String results = dynamicTableService.getAllTableNamesForUser(user);
        System.out.println(results);
        ResponseEntity<?> response = results != null ? ResponseEntity.status(HttpStatus.FOUND).body(results)
                : ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No User Specified");
        return response;
    }

    @PostMapping("/activity")
    @ResponseBody

    public String requestTableActivity(@CookieValue(value = "sessionToken", required = false) String token,
            @RequestBody Map<String, String> request) {
        String timeframae = request.get("timeFrame");
        String tableName = request.get("tableName");
        System.out.println(timeframae);

        User user = token != null ? userService.validateSessionToken(token) : null;
        if (user != null) {
            CollectionTable table = dynamicTableService.findTable(user.getId(), tableName);
            int[] activity = dynamicTableService.getActivity(table, timeframae);
            String[] stringActivity = Arrays.stream(activity).mapToObj(String::valueOf).toArray(String[]::new);
            return String.join(",", stringActivity);
        }
        return "NO CREDENTIALS PROVIDED";
    }

    @PostMapping("/delete")
    @ResponseBody
    public ResponseEntity<?> deleteTable(@CookieValue(value = "sessionToken", required = false) String token,
            @RequestBody Map<String, String> request) {
        User user = token != null ? userService.validateSessionToken(token) : null;
        if (user != null) {
            CollectionTable tableToDelete = dynamicTableService.findTable(user.getId(),
                    request.get("tableName"));

            if (tableToDelete != null) {
                collectionTableRepository.delete(tableToDelete);
                return ResponseEntity.ok().body("DELETED");
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No Credentials provided");

    }

}
