package com.shield.txc;

import com.shield.txc.domain.ShieldEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author snowalker
 * @version 1.0
 * @date 2019/7/30 22:58
 * @className BaseEventRepository
 * @desc 基础事件持久层
 * TODO 表名可配置
 */
public class BaseEventRepository {

    JdbcTemplate jdbcTemplate;

    String tableName;

    public BaseEventRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public BaseEventRepository(String tableName, JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.tableName = tableName;
    }

    /**
     * 插入事件
     *
     * @param event
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean insertEvent(ShieldEvent event) {
        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO shield_event")
                .append("(event_type, tx_type, event_status, content, app_id, biz_key)")
                .append(" VALUES(?,?,?,?,?,?)");
        return jdbcTemplate.update(sqlBuilder.toString(), preparedStatement -> {
            preparedStatement.setString(1, event.getEventType());
            preparedStatement.setString(2, event.getTxType());
            preparedStatement.setString(3, event.getEventStatus());
            preparedStatement.setString(4, event.getContent());
            preparedStatement.setString(5, event.getAppId());
            preparedStatement.setString(6, event.getBizKey());
        }) == 1;
    }

    /**
     * 插入事件带主键id
     *
     * @param event
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean insertEventWithId(ShieldEvent event) {
        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO shield_event")
                .append("(id, event_type, tx_type, event_status, content, app_id, biz_key)")
                .append(" VALUES(?,?,?,?,?,?,?)");
        return jdbcTemplate.update(sqlBuilder.toString(), preparedStatement -> {
            preparedStatement.setInt(1, event.getId());
            preparedStatement.setString(2, event.getEventType());
            preparedStatement.setString(3, event.getTxType());
            preparedStatement.setString(4, event.getEventStatus());
            preparedStatement.setString(5, event.getContent());
            preparedStatement.setString(6, event.getAppId());
            preparedStatement.setString(7, event.getBizKey());
        }) == 1;
    }

    /**
     * 更新事件状态
     *
     * @param event
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateEventStatusById(ShieldEvent event) {
        StringBuilder sqlBuilder = new StringBuilder("UPDATE shield_event SET event_status=? ")
                .append("where id = ? AND event_status=?");
        return jdbcTemplate.update(sqlBuilder.toString(), preparedStatement -> {
            preparedStatement.setString(1, event.getEventStatus());
            preparedStatement.setInt(2, event.getId());
            preparedStatement.setString(3, event.getBeforeUpdateEventStatus());
        }) == 1;
    }

    /**
     * 逻辑删除事件
     *
     * @param id
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteEventLogicallyById(Integer id) {
        StringBuilder sqlBuilder = new StringBuilder("UPDATE shield_event SET record_status=1 ")
                .append("where id = ?");
        return jdbcTemplate.update(sqlBuilder.toString(), preparedStatement -> {
            preparedStatement.setInt(1, id);
        }) == 1;
    }

    /**
     * 根据事件状态获取事件列表
     *
     * @param eventStatus
     * @return
     */
    public List<ShieldEvent> queryEventListByStatus(String eventStatus) {
        final List<ShieldEvent> resultList = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT id, event_type, event_status, tx_type, content,")
                .append("app_id, record_status, gmt_create, gmt_update, biz_key")
                .append(" from shield_event where event_status=? and record_status=0 limit 50");
        jdbcTemplate.query(sqlBuilder.toString(), new Object[]{eventStatus}, resultSet -> {
            ShieldEvent shieldEvent = new ShieldEvent();
            shieldEvent.setId(resultSet.getInt("id"))
                    .setEventType(resultSet.getString("event_type"))
                    .setEventStatus(resultSet.getString("event_status"))
                    .setTxType(resultSet.getString("tx_type"))
                    .setContent(resultSet.getString("content"))
                    .setAppId(resultSet.getString("app_id"))
                    .setRecordStatus(resultSet.getInt("record_status"))
                    .setGmtCreate(resultSet.getTimestamp("gmt_create"))
                    .setGmtUpdate(resultSet.getTimestamp("gmt_update"))
                    .setBizKey(resultSet.getString("biz_key"));
            resultList.add(shieldEvent);
        });
        return resultList;
    }

    /**
     * 查询事件详情
     *
     * @param id
     * @return
     */
    public ShieldEvent queryEventById(int id) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT id, event_type, event_status, tx_type, content,")
                .append("app_id, record_status, gmt_create, gmt_update, biz_key")
                .append(" from shield_event where id=?");
        final ShieldEvent shieldEvent = new ShieldEvent();
        jdbcTemplate.query(sqlBuilder.toString(), new Object[]{id}, resultSet -> {
            shieldEvent.setId(resultSet.getInt("id"))
                    .setEventType(resultSet.getString("event_type"))
                    .setEventStatus(resultSet.getString("event_status"))
                    .setTxType(resultSet.getString("tx_type"))
                    .setContent(resultSet.getString("content"))
                    .setAppId(resultSet.getString("app_id"))
                    .setRecordStatus(resultSet.getInt("record_status"))
                    .setGmtCreate(resultSet.getTimestamp("gmt_create"))
                    .setGmtUpdate(resultSet.getTimestamp("gmt_update"))
                    .setBizKey(resultSet.getString("biz_key"))
                    .setSuccess(Boolean.TRUE);
        });
        return shieldEvent;
    }

    /**
     * 查询事件详情
     * @param bizKey
     * @param txType
     * @param appId
     * @return
     */
    public List<ShieldEvent> queryEventByBizkeyCond(String bizKey, String txType, String appId) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT id, event_type, event_status, tx_type, content,")
                .append("app_id, record_status, gmt_create, gmt_update, biz_key")
                .append(" from shield_event where biz_key=? and tx_type=? and app_id=? ")
                .append(" AND (event_status = 'CONSUME_INIT' OR event_status = 'CONSUME_PROCESSING' OR event_status = 'CONSUME_PROCESSED')");
        final List<ShieldEvent> resultList = new ArrayList<>();
        jdbcTemplate.query(sqlBuilder.toString(), new Object[]{bizKey, txType, appId}, resultSet -> {
            ShieldEvent shieldEvent = new ShieldEvent();
            shieldEvent.setId(resultSet.getInt("id"))
                    .setEventType(resultSet.getString("event_type"))
                    .setEventStatus(resultSet.getString("event_status"))
                    .setTxType(resultSet.getString("tx_type"))
                    .setContent(resultSet.getString("content"))
                    .setAppId(resultSet.getString("app_id"))
                    .setRecordStatus(resultSet.getInt("record_status"))
                    .setGmtCreate(resultSet.getTimestamp("gmt_create"))
                    .setGmtUpdate(resultSet.getTimestamp("gmt_update"))
                    .setBizKey(resultSet.getString("biz_key"))
                    .setSuccess(Boolean.TRUE);
            resultList.add(shieldEvent);
        });
        return resultList;
    }

}
