UPDATE purchase_pattern
SET date_from = NULL,
    date_to = NULL,
    update_date = CURRENT_TIMESTAMP
WHERE pattern_type = 'SEARCH'
  AND (date_from IS NOT NULL OR date_to IS NOT NULL);
