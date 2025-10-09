package com.vitacheck.ai.repository;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

/**
 * Lightweight DAO for catalog-driven prompts.
 * We use native SQL and return rows as Object[] to avoid binding to a specific @Entity.
 * This avoids Spring Data's repository store detection issues in multi-store setups.
 *
 * Row layout for both methods:
 *   [0] Long   supplement_id
 *   [1] String supplement_name
 *   [2] String purpose_names_csv  (lowercased, comma-separated)
 *   [3] String ingredient_names_csv (comma-separated)
 */
@Repository
@Transactional(readOnly = true)
public class SupplementCatalogRepository {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Supplements that have at least one ingredient whose purpose is in :purposeKeys.
     * Returns rows as: [id, name, purpose_names_csv, ingredient_names_csv]
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> findCatalogForPurposesNative(List<String> purposeKeys) {
        String sql = """
            SELECT 
              s.id AS id,
              s.name AS name,
              GROUP_CONCAT(DISTINCT LOWER(p.name) ORDER BY p.name SEPARATOR ',')      AS purpose_names_csv,
              GROUP_CONCAT(DISTINCT i.name        ORDER BY i.name SEPARATOR ',')      AS ingredient_names_csv
            FROM supplements s
            JOIN supplement_ingredients si ON si.supplement_id = s.id
            JOIN ingredients i            ON i.id = si.ingredient_id
            JOIN purpose_ingredients pi   ON pi.ingredient_id = i.id
            JOIN purposes p               ON p.id = pi.purpose_id
            WHERE LOWER(p.name) IN (:purposeKeys)
            GROUP BY s.id, s.name
            ORDER BY COUNT(DISTINCT p.id) DESC, s.id ASC
            """;
        Query q = entityManager.createNativeQuery(sql);
        q.setParameter("purposeKeys", purposeKeys);
        return (List<Object[]>) q.getResultList();
    }

    /**
     * Whole catalog for fallback / prompt quality. No filtering by purposes.
     * Returns rows as: [id, name, purpose_names_csv, ingredient_names_csv]
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> findWholeCatalogNative() {
        String sql = """
            SELECT 
              s.id AS id,
              s.name AS name,
              GROUP_CONCAT(DISTINCT LOWER(p.name) ORDER BY p.name SEPARATOR ',')      AS purpose_names_csv,
              GROUP_CONCAT(DISTINCT i.name        ORDER BY i.name SEPARATOR ',')      AS ingredient_names_csv
            FROM supplements s
            LEFT JOIN supplement_ingredients si ON si.supplement_id = s.id
            LEFT JOIN ingredients i            ON i.id = si.ingredient_id
            LEFT JOIN purpose_ingredients pi   ON pi.ingredient_id = i.id
            LEFT JOIN purposes p               ON p.id = pi.purpose_id
            GROUP BY s.id, s.name
            ORDER BY s.name ASC, s.id ASC
            """;
        Query q = entityManager.createNativeQuery(sql);
        return (List<Object[]>) q.getResultList();
    }
}
