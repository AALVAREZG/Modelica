package es.aalvarez.modelica.service;

import es.aalvarez.modelica.model.Puesto;
import es.aalvarez.modelica.model.PuestoExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PuestoMapper {

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table modlic_puestos
	 * @mbggenerated  Fri Jan 22 20:29:47 CET 2016
	 */
	int countByExample(PuestoExample example);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table modlic_puestos
	 * @mbggenerated  Fri Jan 22 20:29:47 CET 2016
	 */
	int deleteByExample(PuestoExample example);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table modlic_puestos
	 * @mbggenerated  Fri Jan 22 20:29:47 CET 2016
	 */
	int deleteByPrimaryKey(Integer idpuesto);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table modlic_puestos
	 * @mbggenerated  Fri Jan 22 20:29:47 CET 2016
	 */
	int insert(Puesto record);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table modlic_puestos
	 * @mbggenerated  Fri Jan 22 20:29:47 CET 2016
	 */
	int insertSelective(Puesto record);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table modlic_puestos
	 * @mbggenerated  Fri Jan 22 20:29:47 CET 2016
	 */
	List<Puesto> selectByExample(PuestoExample example);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table modlic_puestos
	 * @mbggenerated  Fri Jan 22 20:29:47 CET 2016
	 */
	Puesto selectByPrimaryKey(Integer idpuesto);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table modlic_puestos
	 * @mbggenerated  Fri Jan 22 20:29:47 CET 2016
	 */
	int updateByExampleSelective(@Param("record") Puesto record,
			@Param("example") PuestoExample example);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table modlic_puestos
	 * @mbggenerated  Fri Jan 22 20:29:47 CET 2016
	 */
	int updateByExample(@Param("record") Puesto record,
			@Param("example") PuestoExample example);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table modlic_puestos
	 * @mbggenerated  Fri Jan 22 20:29:47 CET 2016
	 */
	int updateByPrimaryKeySelective(Puesto record);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table modlic_puestos
	 * @mbggenerated  Fri Jan 22 20:29:47 CET 2016
	 */
	int updateByPrimaryKey(Puesto record);
}