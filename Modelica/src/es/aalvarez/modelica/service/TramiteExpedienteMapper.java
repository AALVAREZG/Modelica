package es.aalvarez.modelica.service;

import es.aalvarez.modelica.model.TramiteExpediente;
import es.aalvarez.modelica.model.TramiteExpedienteExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TramiteExpedienteMapper {

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table modlic_tramites
	 * @mbggenerated  Fri Jan 22 20:29:47 CET 2016
	 */
	int countByExample(TramiteExpedienteExample example);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table modlic_tramites
	 * @mbggenerated  Fri Jan 22 20:29:47 CET 2016
	 */
	int deleteByExample(TramiteExpedienteExample example);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table modlic_tramites
	 * @mbggenerated  Fri Jan 22 20:29:47 CET 2016
	 */
	int deleteByPrimaryKey(Integer idtramite);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table modlic_tramites
	 * @mbggenerated  Fri Jan 22 20:29:47 CET 2016
	 */
	int insert(TramiteExpediente record);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table modlic_tramites
	 * @mbggenerated  Fri Jan 22 20:29:47 CET 2016
	 */
	int insertSelective(TramiteExpediente record);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table modlic_tramites
	 * @mbggenerated  Fri Jan 22 20:29:47 CET 2016
	 */
	List<TramiteExpediente> selectByExample(TramiteExpedienteExample example);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table modlic_tramites
	 * @mbggenerated  Fri Jan 22 20:29:47 CET 2016
	 */
	TramiteExpediente selectByPrimaryKey(Integer idtramite);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table modlic_tramites
	 * @mbggenerated  Fri Jan 22 20:29:47 CET 2016
	 */
	int updateByExampleSelective(@Param("record") TramiteExpediente record,
			@Param("example") TramiteExpedienteExample example);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table modlic_tramites
	 * @mbggenerated  Fri Jan 22 20:29:47 CET 2016
	 */
	int updateByExample(@Param("record") TramiteExpediente record,
			@Param("example") TramiteExpedienteExample example);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table modlic_tramites
	 * @mbggenerated  Fri Jan 22 20:29:47 CET 2016
	 */
	int updateByPrimaryKeySelective(TramiteExpediente record);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table modlic_tramites
	 * @mbggenerated  Fri Jan 22 20:29:47 CET 2016
	 */
	int updateByPrimaryKey(TramiteExpediente record);
}