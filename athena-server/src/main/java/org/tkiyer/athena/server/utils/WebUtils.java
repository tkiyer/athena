package org.tkiyer.athena.server.utils;

import org.tkiyer.athena.engine.api.metadata.Catalog;
import org.tkiyer.athena.engine.api.metadata.Schema;
import org.tkiyer.athena.engine.api.metadata.Table;
import org.tkiyer.athena.engine.api.security.AthenaUser;

import javax.servlet.http.HttpSession;
import java.util.*;

public class WebUtils {

    private final static String SESSION_USER = "ARTEMIS_LOGIN_USER";

    /**
     * Map struct. <p />
     *
     * KEY = {catalog}
     */
    private final static String CACHED_CATALOG_LIST = "CACHED_CATALOG_LIST";

    /**
     * Map struct. <p />
     *
     * KEY = {catalog}::{schema}
     */
    private final static String CACHED_SCHEMA_LIST = "CACHED_SCHEMA_LIST";

    /**
     * Map struct. <p />
     *
     * KEY = {catalog}::{schema}::{table}
     */
    private final static String CACHED_TABLE_LIST = "CACHED_TABLE_LIST";

    public static AthenaUser getLoginUser(HttpSession session) {
//        Object obj = session.getAttribute(SESSION_USER);
//        return obj instanceof ArtemisUser ? (ArtemisUser) obj : null;
        return new AthenaUser() {
            @Override
            public String getUser() {
                return "Anonymous";
            }
        };
    }

    public static void cacheCatalogs(List<Catalog> catalogs, HttpSession session) {
        Map<String, Catalog> catalogMap = new HashMap<>();
        for (Catalog catalog : catalogs) {
            catalogMap.put(catalog.getName(), catalog);
        }
        session.setAttribute(CACHED_CATALOG_LIST, catalogMap);
    }

    public static List<Catalog> getCachedCatalogs(HttpSession session) {
        List<Catalog> catalogs = new ArrayList<>(getSessionCachedCatalogMap(session).values());
        Collections.sort(catalogs);
        return catalogs;
    }

    public static Catalog wrapCatalog(String catalog, HttpSession session) {
        Catalog c = getSessionCachedCatalogMap(session).get(catalog);
        if (null == c) {
            throw new IllegalArgumentException(String.format("Catalog[%s] not found from session cache!", catalog));
        }
        return c;
    }

    public static void cacheSchemas(Catalog catalog, List<Schema> schemas, HttpSession session) {
        Map<String, Schema> schemaMap = getSessionCachedSchemaMap(session);
        for (Schema schema : schemas) {
            schema.setCatalog(catalog);
            schemaMap.put(catalog.getName() + "::" +schema.getName(), schema);
        }
        session.setAttribute(CACHED_SCHEMA_LIST, schemaMap);
        // refresh catalog cache
        catalog.setSchemas(schemas);
        getSessionCachedCatalogMap(session).put(catalog.getName(), catalog);
    }

    public static Schema wrapSchema(String catalog, String schema, HttpSession session) {
        return getSessionCachedSchemaMap(session).get(catalog + "::" + schema);
    }


    public static void cacheTables(Schema schema, List<Table> tables, HttpSession session) {
        Map<String, Table> tableMap = getSessionCachedTableMap(session);
        for (Table table : tables) {
            table.setSchema(schema);
            tableMap.put(table.getSchema().getCatalog().getName() + "::" + table.getSchema().getName() + "::" + table.getName(), table);
        }
        session.setAttribute(CACHED_TABLE_LIST, tableMap);
        // refresh schema cache
        schema.setTables(tables);
        getSessionCachedSchemaMap(session).put(schema.getCatalog().getName() + "::" + schema.getName(), schema);
    }

    public static Table wrapTable(String catalog, String schema, String table, HttpSession session) {
        return getSessionCachedTableMap(session).get(catalog + "::" + schema + "::" + table);
    }

    public static void cacheTable(Table table, HttpSession session) {
        getSessionCachedTableMap(session).put(table.getSchema().getCatalog().getName() + "::" + table.getSchema().getName() + "::" + table.getName(), table);
    }

    private static Map<String, Catalog> getSessionCachedCatalogMap(HttpSession session) {
        Object obj = session.getAttribute(CACHED_CATALOG_LIST);
        return obj instanceof Map ? (Map<String, Catalog>) obj : new HashMap<>();
    }

    private static Map<String, Schema> getSessionCachedSchemaMap(HttpSession session) {
        Object obj = session.getAttribute(CACHED_SCHEMA_LIST);
        return obj instanceof Map ? (Map<String, Schema>) obj : new HashMap<>();
    }

    private static Map<String, Table> getSessionCachedTableMap(HttpSession session) {
        Object obj = session.getAttribute(CACHED_TABLE_LIST);
        return obj instanceof Map ? (Map<String, Table>) obj : new HashMap<>();
    }
}
