package master.dao.base.impl;

import master.dao.base.GenericDao;
import master.utils.ReflectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.*;
import org.hibernate.criterion.*;
import org.hibernate.metadata.ClassMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.*;

/**
 * 封装Hibernate原生API的DAO泛型基类. 可在Service层直接使用,也可以扩展泛型DAO子类使用.
 * <p/>
 * <p>
 * To register this class in your Spring context file, use the following XML.
 *
 * @param <T>
 *            DAO操作的对象类型
 * @param <PK>
 *            主键类型
 */
@SuppressWarnings("unchecked")
public class GenericDaoHibernate<T, PK extends Serializable> implements GenericDao<T, PK> {
    //memcached的客户端
    /*@Autowired
    protected JedisPool cache;*/
    //日志
    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected HibernateTemplate hibernateTemplate;
    //session工厂
    protected SessionFactory sessionFactory;

    /**
     * 用于Dao层子类使用的构造函数. 通过子类的泛型定义取得对象类型Class.
     * eg. public class UserDao extends GenericDaoHibernate<User, Long>
     */
    public GenericDaoHibernate() {

    }

    /**
     * 用于省略Dao层, 在Service层直接使用通用GenericDaoHibernate的构造函数. 在构造函数中定义对象类型Class.
     * eg. GenericDaoHibernate<User, Long> userDao =
     *                new GenericDaoHibernate<User, Long>(sessionFactory, User.class);
     */
    public GenericDaoHibernate(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.hibernateTemplate = new HibernateTemplate(sessionFactory);
    }

    /**
     * hibernateTemplate的get.
     */
    public HibernateTemplate getHibernateTemplate() {
        return this.hibernateTemplate;
    }

    /**
     * sessionFactory的get.
     */
    public SessionFactory getSessionFactory() {
        return this.sessionFactory;
    }
    /**
     * sessionFactory 的 set.
     * 采用@Autowired按类型注入SessionFactory, 当有多个SesionFactory的时候Override本函数.
     */
    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.hibernateTemplate = new HibernateTemplate(sessionFactory);
    }

    /**
     * 取得当前绑定到线程中Session,在commit或rollback时会自动关闭.
     */
    public Session getSession() {
        Session session = sessionFactory.getCurrentSession();
        session.setFlushMode(FlushMode.AUTO);
        return session;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<T> getAll() {
        return hibernateTemplate.loadAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<T> getAllDistinct() {
        Collection result = new LinkedHashSet(getAll());
        return new ArrayList(result);
    }

    /**
     * 通过 id 查询
     * {@inheritDoc}
     */

    @Override
    public T get(PK id) {
        Assert.notNull(id, "id不能为空");
        T entity = hibernateTemplate.get(this.persistentClass, id);
        if (entity == null) {
            logger.warn("Uh oh, '" + this.persistentClass
                    + "' object with id '" + id + "' not found...");
            return null;
        }

        return entity;
    }

    /**
     * 通过 id 判断数据是否存在
     * {@inheritDoc}
     */
    @Override
    public boolean exists(PK id) {
        Assert.notNull(id, "id不能为空");
        T entity = hibernateTemplate.get(this.persistentClass, id);
        return entity != null;
    }

    /**
     * 添加
     * {@inheritDoc}
     */
    @Override
    public T save(T object) {
        Assert.notNull(object, "object不能为空");
        logger.debug("save entity: {}", object);
        return (T) getSession().merge(object);
    }

    /**
     * 修改
     * {@inheritDoc}
     */
    @Override
    public void update(T object) {
        Assert.notNull(object, "object不能为空");
        logger.debug("update entity: {}", object);
        this.getSession().clear();
        hibernateTemplate.update(object);
        this.flush();
    }

    /**
     * 通过 id 删除
     * {@inheritDoc}
     */
    @Override
    public void remove(PK id) {
        Assert.notNull(id, "id不能为空");
        hibernateTemplate.delete(this.get(id));
        logger.debug("delete entity {},id is {}", persistentClass
                .getSimpleName(), id);
    }

    /**
     * 删除
     * {@inheritDoc}
     */
    @Override
    public void remove(final T object) {
        Assert.notNull(object, "object不能为空");
        hibernateTemplate.delete(object);
        logger.debug("delete object: {}", object);
    }

    /**
     * 条件查询
     * queryName
     */
    @Override
    public List<T> findByNamedQuery(String queryName,
                                    Map<String, Object> queryParams) {
        String[] params = new String[queryParams.size()];
        Object[] values = new Object[queryParams.size()];

        int index = 0;
        for (String s : queryParams.keySet()) {
            params[index] = s;
            values[index++] = queryParams.get(s);
        }

        return (List<T>) hibernateTemplate.findByNamedQueryAndNamedParam(queryName,
                params, values);
    }

    /**
     * 获取全部对象，支持排序，结果去重复.
     * orderBy：排序条件
     * isAsc：是否升序排列
     */
    @Override
    public List<T> getAll(String orderBy, boolean isAsc) {
        Criteria c = createCriteria();
        if (isAsc) {
            c.addOrder(Order.asc(orderBy));
        } else {
            c.addOrder(Order.desc(orderBy));
        }
        // 去除重复
        return c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
    }

    /**
     * 按属性查找对象列表
     * propertyName：属性名
     * value：属性值
     */
    @Override
    public List<T> findBy(final String propertyName, final Object value) {
        Assert.hasText(propertyName, "propertyName不能为空");
        Criterion criterion = Restrictions.eq(propertyName, value);
        return find(criterion);
    }

    /**
     * 按属性查找唯一对象
     * propertyName：属性名
     * value：属性值
     */
    @Override
    public T findUniqueBy(final String propertyName, final Object value) {
        Assert.hasText(propertyName, "propertyName不能为空");
        Criterion criterion = Restrictions.eq(propertyName, value);
        return (T) createCriteria(criterion).uniqueResult();
    }

    /**
     * 按id 列表获取对象
     */
    @Override
    public List<T> findByIds(List<PK> ids) {
        return find(Restrictions.in(getIdName(), ids));
    }

    /**
     * 获取对象的主键名
     */
    @Override
    public String getIdName() {
        ClassMetadata meta = getSessionFactory().getClassMetadata(persistentClass);
        return meta.getIdentifierPropertyName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <X> List<X> find(final String hql, final Object... values) {
        return createQuery(hql, values).list();
    }

    /**
     * 执行count查询获得本次Hql查询所能获得的对象总数.
     *
     * 本函数只能自动处理简单的hql语句,复杂的hql查询请另行编写count语句查询.
     */
    protected long countHqlResult(final String hql, final Object... values) {
        Long count = 0L;
        String fromHql = hql;
        // select子句与order by子句会影响count查询,进行简单的排除.
        fromHql = "from " + StringUtils.substringAfter(fromHql, "from");
        fromHql = StringUtils.substringBefore(fromHql, "order by");

        String countHql = "select count(*) " + fromHql;

        try {
            count = findUnique(countHql, values);
        } catch (Exception e) {
            throw new RuntimeException("hql can't be auto count, hql is:"
                    + countHql, e);
        }
        return count;
    }

    /**
     * 根据查询HQL与参数列表创建Query对象{@inheritDoc}
     */
    @Override
    public <X> List<X> find(final String hql, final Map<String, ?> values) {
        return createQuery(hql, values).list();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <X> X findUnique(final String hql, final Object... values) {
        return (X) createQuery(hql, values).uniqueResult();
    }

    /**
     * 根据查询HQL与参数列表创建Query对象
     */
    @Override
    public <X> X findUnique(final String hql, final Map<String, ?> values) {
        return (X) createQuery(hql, values).uniqueResult();
    }

    /**
     *	执行HQL进行批量修改/删除操作.
     */
    @Override
    public int batchExecute(final String hql, final Object... values) {
        return createQuery(hql, values).executeUpdate();
    }

    /**
     * 执行HQL进行批量修改/删除操作.
     */
    @Override
    public int batchExecute(final String hql, final Map<String, ?> values) {
        return createQuery(hql, values).executeUpdate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int batchExecuteSQL(final String hql, final Object... values) {
        return createSQLQuery(hql, values).executeUpdate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int batchExecuteSQL(final String hql, final Map<String, ?> values) {
        return createSQLQuery(hql, values).executeUpdate();
    }

    /**
     * 通过sql查询
     * queryString：sql语句
     * paramValues：问号所替代的参数
     */
    @Override
    public List<T> findBySQL(final String queryString,
                             final Object[] paramValues) {

        Query queryObject = getSession().createSQLQuery(queryString);
        // prepareQuery(queryObject);
        if (paramValues != null) {
            for (int i = 0; i < paramValues.length; i++) {
                queryObject.setParameter(i, paramValues[i]);
            }
        }
        return queryObject.list();
    }

    /**
     * 根据查询HQL与参数列表创建Query对象.
     */
    @Override
    public Query createQuery(final String queryString, final Object... values) {
        Assert.hasText(queryString, "queryString不能为空");
        Query query = getSession().createQuery(queryString);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                query.setParameter(i, values[i]);
            }
        }
        return query;
    }

    /**
     * 根据查询HQL与参数列表创建Query对象.
     */
    @Override
    public Query createQuery(final String queryString,
                             final Map<String, ?> values) {
        Assert.hasText(queryString, "queryString不能为空");
        Query query = getSession().createQuery(queryString);
        if (values != null) {
            query.setProperties(values);
        }
        return query;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SQLQuery createSQLQuery(final String queryString,
                                   final Object... values) {
        Assert.hasText(queryString, "queryString不能为空");
        SQLQuery query = getSession().createSQLQuery(queryString);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                query.setParameter(i, values[i]);
            }
        }
        return query;
    }

    /**
     * 创建SQLQuery
     */
    @Override
    public SQLQuery createSQLQuery(final String queryString,
                                   final Map<String, ?> values) {
        Assert.hasText(queryString, "queryString不能为空");
        SQLQuery query = getSession().createSQLQuery(queryString);
        if (values != null) {
            query.setProperties(values);
        }
        return query;
    }

    /**
     * 根据Criterion条件创建Criteria.
     */
    @Override
    public List<T> find(final Criterion... criterions) {
        return createCriteria(criterions).setResultTransformer(
                Criteria.DISTINCT_ROOT_ENTITY).list();
    }

    /**
     * 按Criteria查询唯一对象.
     */
    @Override
    public T findUnique(final Criterion... criterions) {
        return (T) createCriteria(criterions).uniqueResult();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Criteria createCriteria(final Criterion... criterions) {
        Criteria criteria = getSession().createCriteria(persistentClass);
        for (Criterion c : criterions) {
            criteria.add(c);
        }
        return criteria;
    }

    /**
     * Flush当前Session.
     */
    @Override
    public void flush() {
        hibernateTemplate.flush();
        getSession().flush();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query distinct(Query query) {
        query.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        return query;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Criteria distinct(Criteria criteria) {
        criteria
                .setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        return criteria;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<T> findByNum(final String queryString, final Object paramValue,
                             final int start, final int length) {
        return findByNum(queryString,
                paramValue != null ? new Object[] { paramValue }
                        : (Object[]) null, start, length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<T> findByNum(final String queryString, final int start,
                             final int length) {
        return findByNum(queryString, (Object[]) null, start, length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<T> findByNum(final String queryString, final List<Object> list,
                             final int start, final int length) {
        Object[] objects = new Object[list.size()];
        for (int a = 0; a < list.size(); a++) {
            objects[a] = list.get(a);
        }
        return findByNum(queryString, objects, start, length);
    }

    protected List<T> findByNum_bak(final String queryString,
                                    final Object[] paramValues, final int start, final int length) {
        Query queryObject = createQuery(queryString);
        if (start >= 0) {
            queryObject.setFirstResult(start);
        }
        if (length > 0) {
            queryObject.setMaxResults(length);
        }
        if (paramValues != null) {
            for (int i = 0; i < paramValues.length; i++) {
                queryObject.setParameter(i, paramValues[i]);
            }
        }
        return queryObject.list();
    }

    /**
     * 分页查询；Spring HibernateTemplate.find()方法不支持分页设置
     * (有maxResults,无firstResult),本方法完全保留Spring特性实现分页查询；
     * 本方法为protected，不是Dao接口方法，避免扩散到service层
     *
     * @param queryString
     *            查询语句
     * @param paramValues
     *            查询参数值
     * @param start
     *            结果集起始位置,默认从0开始
     * @param length
     *            结果集长度,默认为0即全部
     * @return
     */
    protected List<T> findByNum(final String queryString,
                                final Object[] paramValues, final int start, final int length) {

        Query queryObject = getSession().createQuery(queryString);
        // prepareQuery(queryObject);
        if (start >= 0)
            queryObject.setFirstResult(start);
        if (length > 0)
            queryObject.setMaxResults(length);
        if (paramValues != null) {
            for (int i = 0; i < paramValues.length; i++) {
                queryObject.setParameter(i, paramValues[i]);
            }
        }
        return queryObject.list();

    }

    @Override
    public long getAmount(String queryString) {
        return getAmount(queryString, (Object[]) null);
    }

    @Override
    public long getAmount(String queryString, Object paramValue) {
        return getAmount(queryString,
                paramValue != null ? new Object[] { paramValue }
                        : (Object[]) null);
    }

    @Override
    public long getAmount(final String queryString, final List<Object> list) {
        Object[] objects = new Object[list.size()];
        for (int a = 0; a < list.size(); a++) {
            objects[a] = list.get(a);
        }
        return getAmount(queryString, objects);
    }

    /**
     * 查询满足查询条件的结果长度<br/>
     *
     * @param queryString
     *            查询语句，需要确认字符串包含 "select count"...
     * @param paramValues
     *            查询参数值
     * @return
     */
    protected long getAmount(String queryString, Object[] paramValues) {
        Long count = 0L;
        try {
            count = findUnique(queryString, paramValues);
        } catch (Exception e) {
            throw new RuntimeException("hql can't be auto count, hql is:"
                    + queryString, e);
        }
        return count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T findOne(final String queryString) {
        return findOne(queryString, new ArrayList<Object>());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T findOne(String queryString, final Object paramValue) {
        List objs = findByNum(queryString,
                paramValue != null ? new Object[] { paramValue }
                        : (Object[]) null, 0, 0);
        if (objs != null && objs.size() > 0) {
            return (T) objs.get(0);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T findOne(final String queryString, final List<Object> list) {
        Object[] objects = new Object[list.size()];
        for (int a = 0; a < list.size(); a++) {
            objects[a] = list.get(a);
        }
        List objs = findByNum(queryString, objects, 0, 0);
        if (objs != null && objs.size() > 0) {
            return (T) objs.get(0);
        }
        return null;
    }

    /**
     * 执行count查询获得本次Hql查询所能获得的对象总数.
     *
     * 本函数只能自动处理简单的hql语句,复杂的hql查询请另行编写count语句查询.
     */
    protected long countHqlResult(final String hql, final Map<String, ?> values) {
        String fromHql = hql;
        // select子句与order by子句会影响count查询,进行简单的排除.
        fromHql = "from " + StringUtils.substringAfter(fromHql, "from");
        fromHql = StringUtils.substringBefore(fromHql, "order by");

        String countHql = "select count(*) " + fromHql;

        try {
            Long count = findUnique(countHql, values);
            return count;
        } catch (Exception e) {
            throw new RuntimeException("hql can't be auto count, hql is:"
                    + countHql, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPropertyUnique(final String propertyName,
                                    final Object newValue, final Object oldValue) {
        if (newValue == null || newValue.equals(oldValue)) {
            return true;
        }
        Object object = findUniqueBy(propertyName, newValue);
        return (object == null);
    }

    /**
     * 执行sql语句的删除
     *
     * @param delsql
     */
    protected void deleteBySql(String delsql) {
        this.getSession().createSQLQuery(delsql).executeUpdate();
        this.getSession().flush(); // 清理缓存，执行批量插入
        this.getSession().clear(); // 清空缓存中的 对象
    }
    /**
     * @Title: generateCriteria
     * @Description: 创建查询实例
     * @param clazz 实体类
     * @param orders 排序规则
     * @param criterions 过滤条件
     * @return Criteria    返回类型
     * @throws
     */
    private Criteria generateCriteria(Class<?> clazz, List<Order> orders, List<Criterion> criterions) {
        Criteria crit = this.getSession().createCriteria(clazz);
        // 去除重复数据
        crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        //添加排序规则
        if (orders == null) {
            orders = new ArrayList<Order>();
        }
        for (Order order : orders) {
            if (order != null) {
                crit.addOrder(order);
            }
        }

        //添加过滤条件
        if (criterions == null) {
            criterions = new ArrayList<Criterion>();
        }
        for (Criterion criterion : criterions) {
            if (criterion != null) {
                crit.add(criterion);
            }
        }

        return crit;
    }
}