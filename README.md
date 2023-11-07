# data-copy-batch

## 概要
２つのDBに接続し、test_db1 の Sample テーブルから test_db2 の Sample テーブルへデータをコピーするプログラム。  

## Sampleテーブル
IDと名前を持っているだけのサンプルテーブル 

| カラム名 | 型    | 概要       |
|------|------|----------|
| id   | int  | プライマリーキー |
| name | text | 名前       |

## テストの動かし方
テストクラスは下記ディレクトリ配下に置いてある。  
`data-copy-batch/src/test/`

テスト実行時は、H2DBに接続している。  
あらかじめ必要なH2DBの接続設定は下記ファイルに記載しているので動かしたいテストをすぐに実行できる。  
`data-copy-batch/src/test/resources/application.yml`

動かしたいテストのクラスを右クリックして
`xxxxTestの実行`を行えばテストが実行される。

## アプリケーションの動かし方
アプリケーション実行時は、MySQLに接続している。  
Docker Desktop をインストールし、下記ファイルをデプロイするとMySQLが立ち上がるようになってる。  
`data-copy-batch/docker/docker-compose.yml`
必要なMySQLの接続設定は下記ファイルに記載している。  
`data-copy-batch/src/test/resources/application.yml`

### docker-compose.yml のデプロイ方法
- Docker Desktop をインストールする。  
[Windows に Docker Desktop をインストール](https://docs.docker.jp/docker-for-windows/install.html)  
[Mac に Docker Desktop をインストール](https://docs.docker.jp/desktop/install/mac-install.html)
- IntelliJ の プラグイン `Docker` をインストールする。
[Docker](https://plugins.jetbrains.com/plugin/7724-docker)
- デプロイ
`data-copy-batch/docker/docker-compose.yml` を右クリックして  
`docker:Composeデプロイの実行`を行えばデプロイされる

### init.sql について
docker-compose.yml のデプロイ時に下記ファイルに書かれたSQLが実行される。
`data-copy-batch/docker/init_scripts/init.sql`  
- test_db1 と test_db2 を作成し、test ユーザに権限を付与する
```SQL
CREATE DATABASE IF NOT EXISTS `test_db1`;
GRANT ALL PRIVILEGES ON `test_db1`.* TO `test`@`%`;
CREATE DATABASE IF NOT EXISTS `test_db2`;
GRANT ALL PRIVILEGES ON `test_db2`.* TO `test`@`%`;
```
- test_db1 に sample テーブルを作成し、テストデータを作成する。
```SQL
use test_db1;

CREATE TABLE `sample` (
`id` int UNIQUE NOT NULL,
`name` text
);

INSERT INTO sample VALUES(1, 'name1');
INSERT INTO sample VALUES(2, NULL);
INSERT INTO sample VALUES(3, 'name3');
```
- test_db2 に sample テーブルを作成する。
```SQL
use test_db2;

CREATE TABLE `sample` (
`id` int UNIQUE NOT NULL,
`name` text
);
```
## Spring boot で Java プログラムを動かす
バッチ処理を行うサービス`DataCopyService`を作成しておき`main`処理で下記のように  
`ConfigurableApplicationContext` を使って`Bean`を取得してプログラムを実行することで  
Javaプログラムとして動かすことができる。
```Java
public static void main(String[] args){
    try(ConfigurableApplicationContext context=
        SpringApplication.run(DataCopyBatchApplication.class,args)){
    DataCopyService dataCopyService=context.getBean(DataCopyService.class);
    dataCopyService.copy();
    }
}
```
## ２つのDBに接続する
### application.yml
`application.yml` に接続したいDBの情報を２つ記載しておく。  
コピー元のDBの情報を`copy-source`に、コピー先のDBの情報を`copy-to`に記載する。
```yml
spring:
  datasource:
    copy-source:
      driver-class-name: com.mysql.cj.jdbc.Driver
      url: jdbc:mysql://localhost:63306/test_db1?userCursorFetch=true
      username: test
      password: testpass
      fetchsize: 1000
    copy-to:
      driver-class-name: com.mysql.cj.jdbc.Driver
      url: jdbc:mysql://localhost:63306/test_db2?userCursorFetch=true
      username: test
      password: testpass
      fetchsize: 1000
```
### Configuration
`application.yml` に記載した情報を取得する`Configuration`クラスを作成する。  
コピー元のDBの情報を取得する`CopySourceConfiguration`とコピー先の情報を取得する`CopyToConfiguration`を作成する。
同じクラスのBeanを生成するので、どちらのBeanを生成するか識別できるようにBeanに名前をつける。
```Java
@lombok.Getter
@lombok.Setter
@Component
@ConfigurationProperties(prefix = "spring.datasource.copy-source")
public class CopySourceConfiguration {
    private String driverClassName;
    private String url;
    private String username;
    private String password;
    private int fetchsize;

    @Bean(name = "copySourceDataSource")
    public DataSource createDataSource() {
        return DataSourceBuilder.create()
                .driverClassName(driverClassName)
                .url(url)
                .username(username)
                .password(password)
                .build();
    }
    
    @Bean(name = "copySourceNamedParameterJdbcTemplate")
    public NamedParameterJdbcTemplate createNamedParameterJdbcTemplate(
            @Qualifier("copySourceDataSource") DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Bean(name = "copySourceFetchSize")
    public Integer createFetchSize() {
        return Integer.valueOf(fetchsize);
    }
    
}
```
```Java
@lombok.Getter
@lombok.Setter
@Component
@ConfigurationProperties(prefix = "spring.datasource.copy-to")
public class CopyToConfiguration {
    private String driverClassName;
    private String url;
    private String username;
    private String password;
    private int fetchsize;

    @Bean(name = "copyToDataSource")
    public DataSource createDataSource() {
        return DataSourceBuilder.create()
                .driverClassName(driverClassName)
                .url(url)
                .username(username)
                .password(password)
                .build();
    }
    
    @Bean(name = "copyToNamedParameterJdbcTemplate")
    public NamedParameterJdbcTemplate createNamedParameterJdbcTemplate(
            @Qualifier("copyToDataSource") DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Bean(name = "copyToFetchSize")
    public Integer createFetchSize() {
        return Integer.valueOf(fetchsize);
    }
    
}
```
コピー元のDBから情報を取得する`CopySourceSampleRepositoryImpl`とコピー先のDBから情報を取得する`CopyToSampleRepositoryImpl`を作成する。
`Autowired`するときに`@Qualifier`を利用してどのBeanを受け取るか指定する。
```Java
@Repository()
public class CopySourceSampleRepositoryImpl implements CopySourceSampleRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final Integer fetchSize;
    
    @Autowired
    public CopySourceSampleRepositoryImpl(
            @Qualifier("copySourceNamedParameterJdbcTemplate")
            NamedParameterJdbcTemplate namedParameterJdbcTemplate,
            @Qualifier("copySourceFetchSize") Integer fetchSize) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.fetchSize = fetchSize;
    }

    @Override
    public void truncate() {
        String sql = "TRUNCATE TABLE sample";
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource());
    }

    @Override
    public Optional<SampleModel> selectById(int id) {
        String sql = "SELECT * FROM sample WHERE id = :id";
        SampleRowMapper rowMapper = new SampleRowMapper();
        SqlParameterSource parameters = new MapSqlParameterSource().addValue("id", id);
        List<SampleModel> list = namedParameterJdbcTemplate.query(sql, parameters, rowMapper);
        if (list.size() > 0) {
            return Optional.of(list.get(0));
        }
        return Optional.empty();
    }

    @Override
    public SelectModel<SampleModel> select(int offset, int limit) {
        SelectModel<SampleModel> selectModel =
                SelectModel.<SampleModel>builder()
                        .list(new ArrayList<SampleModel>())
                        .offset(0)
                        .limit(0)
                        .total(0)
                        .build();

        if (offset < 0 || limit < 1) {
            return selectModel;
        }

        String sql = "SELECT * FROM sample ORDER BY id";
        SqlParameterSource parameterSource = new MapSqlParameterSource();
        SampleRowCountCallbackHandler handler = new SampleRowCountCallbackHandler(offset, limit);
        namedParameterJdbcTemplate.getJdbcTemplate().setFetchSize(fetchSize);
        namedParameterJdbcTemplate.query(sql, parameterSource, handler);

        return SelectModel.<SampleModel>builder()
                .list(handler.getList())
                .offset(offset)
                .limit(limit)
                .total(handler.getRowCount())
                .build();

    }

    @Override
    public int insert(SampleModel model) {
        if (ObjectUtils.isEmpty(model)) {
            return 0;
        }

        String sql = "INSERT INTO sample ( " +
                "id, " +
                "name " +
                " ) VALUES ( " +
                ":id, " +
                ":name" +
                " ) ";

        SqlParameterSource parameters = new MapSqlParameterSource("id", model.getId())
                .addValue("name", model.getName().orElse(null));

        return namedParameterJdbcTemplate.update(sql, parameters);
    }
}
```
```Java
@Repository()
public class CopyToSampleRepositoryImpl implements CopyToSampleRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final Integer fetchSize;

    @Autowired
    public CopyToSampleRepositoryImpl(
            @Qualifier("copyToNamedParameterJdbcTemplate")
            NamedParameterJdbcTemplate namedParameterJdbcTemplate,
            @Qualifier("copyToFetchSize") Integer fetchSize) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.fetchSize = fetchSize;
    }


    @Override
    public void truncate() {
        String sql = "TRUNCATE TABLE sample";
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource());
    }

    @Override
    public Optional<SampleModel> selectById(int id) {
        String sql = "SELECT * FROM sample WHERE id = :id";
        SampleRowMapper rowMapper = new SampleRowMapper();
        SqlParameterSource parameters = new MapSqlParameterSource().addValue("id", id);
        List<SampleModel> list = namedParameterJdbcTemplate.query(sql, parameters, rowMapper);
        if (list.size() > 0) {
            return Optional.of(list.get(0));
        }
        return Optional.empty();
    }

    @Override
    public SelectModel<SampleModel> select(int offset, int limit) {
        SelectModel<SampleModel> selectModel =
                SelectModel.<SampleModel>builder()
                        .list(new ArrayList<SampleModel>())
                        .offset(0)
                        .limit(0)
                        .total(0)
                        .build();

        if (offset < 0 || limit < 1) {
            return selectModel;
        }

        String sql = "SELECT * FROM sample ORDER BY id";
        SqlParameterSource parameterSource = new MapSqlParameterSource();
        SampleRowCountCallbackHandler handler = new SampleRowCountCallbackHandler(offset, limit);
        namedParameterJdbcTemplate.getJdbcTemplate().setFetchSize(fetchSize);
        namedParameterJdbcTemplate.query(sql, parameterSource, handler);

        return SelectModel.<SampleModel>builder()
                .list(handler.getList())
                .offset(offset)
                .limit(limit)
                .total(handler.getRowCount())
                .build();

    }

    @Override
    public int insert(SampleModel model) {
        if (ObjectUtils.isEmpty(model)) {
            return 0;
        }

        String sql = "INSERT INTO sample ( " +
                "id, " +
                "name " +
                " ) VALUES ( " +
                ":id, " +
                ":name" +
                " ) ";

        SqlParameterSource parameters = new MapSqlParameterSource("id", model.getId())
                .addValue("name", model.getName().orElse(null));

        return namedParameterJdbcTemplate.update(sql, parameters);
    }
}
```
