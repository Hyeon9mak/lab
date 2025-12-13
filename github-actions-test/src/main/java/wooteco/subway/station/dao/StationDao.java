package wooteco.subway.station.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import wooteco.subway.exception.DuplicateStationNameException;
import wooteco.subway.exception.NoSuchStationException;
import wooteco.subway.station.domain.Station;

@Repository
public class StationDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    private final RowMapper<Station> rowMapper = (rs, rowNum) -> {
        Long foundId = rs.getLong("id");
        String name = rs.getString("name");

        return new Station(foundId, name);
    };

    public StationDao(JdbcTemplate jdbcTemplate, DataSource source) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(source)
            .withTableName("STATION")
            .usingGeneratedKeyColumns("id");
    }

    public Station insert(Station station) {
        try {
            Map<String, String> params1 = new HashMap<>();
            params1.put("name", station.getName());

            SqlParameterSource params2 = new MapSqlParameterSource()
                .addValue("name", station.getName());

            SqlParameterSource params3 = new BeanPropertySqlParameterSource(station);

            Long id = jdbcInsert.executeAndReturnKey(params3).longValue();
            return new Station(id, station.getName());
        } catch (Exception e) {
            throw new DuplicateStationNameException();
        }
    }

    public List<Station> findAll() {
        String sql = "SELECT * FROM STATION";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Station findById(Long id) {
        try {
            String sql = "SELECT * FROM STATION WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, rowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NoSuchStationException();
        }
    }

    public int delete(Long id) {
        String sql = "DELETE FROM STATION WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    public StationDao dropTable() {
        return new StationDao(jdbcTemplate, jdbcInsert.getJdbcTemplate().getDataSource());
    }
}