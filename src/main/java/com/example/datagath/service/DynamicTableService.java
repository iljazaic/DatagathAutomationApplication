package com.example.datagath.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.datagath.util.Stats;
import com.example.datagath.dto.TableCreationResponse;
import com.example.datagath.model.CollectionTable;
import com.example.datagath.model.TableToken;
import com.example.datagath.model.User;
import com.example.datagath.repository.CollectionTableRepository;
import com.example.datagath.repository.TableTokenRepository;
import com.example.datagath.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;

@Service
public class DynamicTableService {

    private EntityManager entityManager;
    private CollectionTableRepository tableRepository;
    private TableTokenRepository tableTokenRepository;
    private UserRepository userRepository;

    public DynamicTableService(EntityManager entityManager, CollectionTableRepository tableRepository,
            TableTokenRepository tableTokenRepository,
            UserRepository userRepository) {
        this.entityManager = entityManager;
        this.tableRepository = tableRepository;
        this.tableTokenRepository = tableTokenRepository;
        this.userRepository = userRepository;
    }

    public Boolean verifySqlLiterals(String string) {
        return string.matches("[a-zA-Z_][a-zA-Z0-9_]*");
    }

    public TableToken createNewTableToken(User user, CollectionTable table) {
        if (user != null) {
            Instant now = Instant.now();
            TableToken newToken = new TableToken(
                    user.getId(),
                    now.plus(365, ChronoUnit.DAYS), // make it reasonable later
                    now,
                    now,
                    table.getName());
            TableToken TOKEN = tableTokenRepository.save(newToken);
            return TOKEN;
        }

        return null;
    }

    public CollectionTable validateTableToken(String token) {

        TableToken t = tableTokenRepository.findByToken(token).orElse(null);

        Instant now = Instant.now();
        if (t != null) {
            if (now.isBefore(t.getExpiryDate())) {

                User user = userRepository.findById(t.getUserId()).orElse(null);

                if (user != null) {
                    return findTable(user.getId(), t.getTableName());
                }
            }

        }
        return null;
    }

    @Transactional
    public TableCreationResponse createTable(String tableName, Long ownerId, Map<String, String> columns) {
        TableCreationResponse response = new TableCreationResponse();
        try {
            CollectionTable table = new CollectionTable(tableName, ownerId, "data");
            table.setUrl(columns);
            System.out.println(table.getUrl());
            String tableId = table.getId();
            tableRepository.save(table);
            // SQL QUERY
            StringBuilder sql = new StringBuilder("CREATE TABLE `" + tableId + "`(");
            columns.forEach((name, type) -> {
                if (verifySqlLiterals(name) && verifySqlLiterals(type)) {
                    sql.append(name).append(" ").append(type).append(", ");
                } else {
                    // response.setSuccess(false);
                    // response.setBody("Please make sure your column names are only
                    // [a-z][A-Z][0-9]");
                    throw new IllegalArgumentException("SQL injection imminent, shutting down");
                }
            });
            sql.append("tmstp DATETIME);");

            entityManager.createNativeQuery(sql.toString()).executeUpdate();

            TableToken tableToken = createNewTableToken(userRepository.findById(ownerId).orElse(null), table);
            response.setTableToken(tableToken);
            response.setSuccess(true);
        } catch (Exception e) {
            System.out.println(e);
            response.setSuccess(false);
            if (e.getClass().getCanonicalName().equals(IllegalArgumentException.class.getCanonicalName())) {
                response.setBody("Column names can only contain letters and numbers");
                ;
            } else {
                response.setBody("Internal Server Error/Network error. Please try again later or report the issue.");

            }
            response.setTableToken(null);
        }
        return response;// too lazy to make errors make sense in case something goes wrong
        // ill just sit there for hours debugging this hating my past self..
        // hi future me its okay hate me im rlly sleepy
    }

    public CollectionTable findTable(Long userId, String name) {
        List<CollectionTable> tableList = tableRepository.findByOwnerId(userId);
        for (CollectionTable collectionTable : tableList) {
            if (collectionTable.getName() != null && collectionTable.getName().equals(name)) {
                return collectionTable;
            }
        }
        return null;
    }

    public CollectionTable findTable(String tableId) {
        return tableRepository.findById(tableId).orElse(null);
    }// override if given table id

    @Transactional
    public int insertValueIntoTable(String tableName, Long userId, Map<String, String> values) {
        CollectionTable collectionTable = tableName != null && userId != null ? findTable(userId, tableName) : null;
        if (collectionTable != null) {
            tableName = collectionTable.getId();
            StringBuilder columnsPart = new StringBuilder();
            StringBuilder valuesPart = new StringBuilder();
            List<Object> params = new ArrayList<>();

            for (Map.Entry<String, String> entry : values.entrySet()) {
                if (!verifySqlLiterals(entry.getKey())) {
                    return 0;
                }
                columnsPart.append(entry.getKey()).append(", ");
                valuesPart.append("?, ");
                params.add(entry.getValue());
            }
            if (columnsPart.length() > 0) {
                columnsPart.setLength(columnsPart.length() - 2);
                valuesPart.setLength(valuesPart.length() - 2);
            }
            tableName = "`" + tableName + "`";
            String sql = "INSERT INTO " + tableName + " (" + columnsPart + ",tmstp) VALUES (" + valuesPart + ","
                    + "?)";
            Query query = entityManager.createNativeQuery(sql);
            for (int i = 0; i < params.size(); i++) {
                query.setParameter(i + 1, params.get(i));
            }
            query.setParameter(params.size() + 1, Timestamp.valueOf(LocalDateTime.now()));
            return query.executeUpdate();
        }
        return 0;
    }

    public String toEchartsOption(List<Map<String, Object>> results) throws Exception {
        List<List<Object>> seriesData = new ArrayList<>();

        for (Map<String, Object> row : results) {
            List<Object> point = new ArrayList<>();
            point.add(row.get("col0").toString()); // timestamp
            point.add(row.get("col1")); // value
            seriesData.add(point);
        }

        Map<String, Object> option = new HashMap<>();
        option.put("tooltip", new HashMap<>());

        Map<String, Object> xAxis = new HashMap<>();
        xAxis.put("type", "time");

        Map<String, Object> yAxis = new HashMap<>();
        yAxis.put("type", "value");

        Map<String, Object> seriesEntry = new HashMap<>();
        seriesEntry.put("type", "line");
        seriesEntry.put("data", seriesData);

        option.put("xAxis", xAxis);
        option.put("yAxis", yAxis);
        option.put("series", List.of(seriesEntry));

        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(option);
    }

    public String formatTableIntoEcharts(String tableName, Long ownerId) {
        CollectionTable collectionTable = ownerId != null && tableName != null ? findTable(ownerId, tableName) : null;
        if (collectionTable != null) {
            String sql = "SELECT  FROM " + collectionTable.getId();
            Query query = entityManager.createNativeQuery(sql);

            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();

            List<Map<String, Object>> mappedResults = results.stream()
                    .map(row -> {
                        Map<String, Object> map = new HashMap<>();
                        Object[] rowArr = (Object[]) row;
                        for (int i = 0; i < rowArr.length; i++) {
                            map.put("col" + i, rowArr[i]);
                        }
                        return map;
                    })
                    .toList();
            try {
                return toEchartsOption(mappedResults);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    private Map<String, String> switchColumnType(String colName, String colType, String tableId) {
        String sql;
        Query query;
        Map<String, String> returning = new HashMap<String, String>();
        switch (colType) {
            case "int":
            case "float":

                // sql = "SELECT AVG(" + colName + ") FROM " + tableId;
                // query = entityManager.createNativeQuery(sql);

                sql = "SELECT " + colName + " FROM " + tableId;
                query = entityManager.createNativeQuery(sql);

                @SuppressWarnings("unchecked")
                List<Number> results = query.getResultList(); // no Object[] needed
                List<Double> values = results.stream()
                        .map(Number::doubleValue)
                        .toList();

                returning.put("average", String.valueOf(Stats.mean(values)));
                returning.put("median", String.valueOf(Stats.median(values)));
                returning.put("mode", String.valueOf(Stats.mode(values)));
                returning.put("std", String.valueOf(Stats.stdDev(values)));
                returning.put("count", String.valueOf(values.size()));

                return returning;
            case "text":
                // sql = "SELECT AVG(" + colName + ") FROM " + tableId;
                // query = entityManager.createNativeQuery(sql);

                sql = "SELECT " + colName + " FROM " + tableId;
                query = entityManager.createNativeQuery(sql);

                @SuppressWarnings("unchecked")
                List<Object> resultsText = query.getResultList();

                List<String> valueStrings = resultsText.stream()
                        .filter(Objects::nonNull)
                        .map(row -> {
                            if (row instanceof Object[]) {
                                Object[] arr = (Object[]) row;
                                return arr.length > 0 && arr[0] != null ? arr[0].toString() : null;
                            } else {
                                return row.toString();
                            }
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()); // use collect in Java <16

                return Stats.top10Strings(valueStrings);
            default:
                return Collections.emptyMap();
        }
    };

    @SuppressWarnings({ "unchecked" })
    public Map<String, Map<String, String>> tableSummary(CollectionTable collectionTable) {
        // CollectionTable collectionTable = ownerId != null && tableName != null ?
        // findTable(ownerId, tableName) : null;
        if (collectionTable != null) {
            Query query = entityManager.createNativeQuery(
                    "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = :tableName");
            query.setParameter("tableName", collectionTable.getId());// get column names and datatype
            List<Object[]> columns = query.getResultList();
            Map<String, Map<String, String>> summaryMap = new HashMap<>();
            for (Object[] col : columns) {
                Map<String, String> summary = switchColumnType(col[0].toString(), col[1].toString(),
                        "`" + collectionTable.getId() + "`");
                summaryMap.put(col[0].toString(), summary);
            }
            return summaryMap;
        } else {
            return Collections.emptyMap();
        }
    }

    public String exportTableToCsv(String tableName, Long ownerId) {
        CollectionTable collectionTable = ownerId != null && tableName != null ? findTable(ownerId, tableName) : null;
        if (collectionTable != null) {
            String sql = "SELECT * FROM `" + collectionTable.getId() + "`";//heavy oh heavy
            Query query = entityManager.createNativeQuery(sql);

            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();
            File folder = new File("../fileBufferStorage");
            if (!folder.exists()) {
                folder.mkdirs(); // in case the directory no existo
            }
            try (FileWriter writer = new FileWriter("../fileBufferStorage/" + collectionTable.getId() + ".csv")) {
                for (Object[] row : results) {
                    for (int i = 0; i < row.length; i++) {
                        if (row[i] != null) {
                            String value = row[i].toString().replace("\"", "\"\"");
                            writer.append("\"").append(value).append("\"");
                        }
                        if (i < row.length - 1) {
                            writer.append(",");
                        }
                    }
                    writer.append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
                return "error";
            }
            return "../fileBufferStorage/" + collectionTable.getId() + ".csv";

        } else {
            return "error";
        }
    }

    public String getAllTableNamesForUser(User user) {
        if (user != null) {
            List<CollectionTable> tableList = tableRepository.findByOwnerId(user.getId());
            String toRespond = "";
            for (CollectionTable collectionTable : tableList) {
                System.out.println(collectionTable.getName());
                toRespond = String.join(",", toRespond, collectionTable.getName());
            }
            return toRespond;
        }
        return null;
    }

    public int[] getActivity(CollectionTable table, String timeframe) {
        String sql;
        int[] buckets;
        switch (timeframe) {
            case "hour":
                sql = """
                        SELECT FLOOR(TIMESTAMPDIFF(MINUTE, tmstp, NOW()) / 5) AS bucket, COUNT(*) AS cnt
                        FROM `%s`
                        WHERE tmstp > NOW() - INTERVAL 1 HOUR
                        GROUP BY bucket;
                        """.formatted(table.getId());
                buckets = new int[12];
                break;
            case "week":
                sql = """
                        SELECT FLOOR(TIMESTAMPDIFF(DAY, tmstp, NOW()) / 1) AS bucket, COUNT(*) AS cnt
                        FROM `%s`
                        WHERE tmstp > NOW() - INTERVAL 7 DAY
                        GROUP BY bucket;
                        """.formatted(table.getId());
                buckets = new int[7];
                break;
            case "month":
                sql = """
                        SELECT FLOOR(TIMESTAMPDIFF(DAY, tmstp, NOW()) / 1) AS bucket, COUNT(*) AS cnt
                        FROM `%s`
                        WHERE tmstp > NOW() - INTERVAL 30 DAY
                        GROUP BY bucket;
                        """.formatted(table.getId());
                buckets = new int[30];

                break;
            case "day":
            default:
                sql = """
                        SELECT FLOOR(TIMESTAMPDIFF(HOUR, tmstp, NOW()) / 1) AS bucket, COUNT(*) AS cnt
                        FROM `%s`
                        WHERE tmstp > NOW() - INTERVAL 1 DAY
                        GROUP BY bucket;
                        """.formatted(table.getId());
                buckets = new int[24];

                break;
        }
        Query query = entityManager.createNativeQuery(sql);

        @SuppressWarnings("unchecked")
        List<Object[]> resultsText = query.getResultList();

        for (Object[] row : resultsText) {
            Integer bucket = ((Number) row[0]).intValue();
            Integer count = ((Number) row[1]).intValue();
            int position = buckets.length - 1 - bucket;
            if (position >= 0 && position < buckets.length) {
                buckets[position] = count;
            }
        }
        return buckets;
    };
}
