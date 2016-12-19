#**Simorm**
This open source project is a simple Object Relationship Mapping to Android, that provides simple and powerful classes for interact with SQLite Databases.

####[Download simorm.jar](https://github.com/delacrmi/android_orm/releases)

####Download from Gradle [![](https://jitpack.io/v/delacrmi/android_orm.svg)](https://jitpack.io/#delacrmi/android_orm)

#####Add it in your root build.gradle at the end of repositories:
```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

#####Add the dependency
```
dependencies {
	compile 'com.github.delacrmi:android_orm:v0.0.2-beta'
}
```

##Creating the Data Base

To create a **Data Base** you need to use the **EntityManager** Class.

```java
//setup the EntityManager
EntityManager manager = new EntityManager(context, dbName, cursorFactory, dbVersion);

//Adding a new persistence
manager.addEntity(Entity.class);

//Starting the manager
manager.init();
```
**Other ways**
```java
EntityManager manager = new EntityManager();
manager.setContext(context);
manager.setDbName(dbName)
manager.setDbVersion(dbVersion)
manager.setFactory(cursorFactory)
manager.addEntity(Entity.class)
manager.init();

Or

new EntityManager()
	.setContext(context)
    .setDbName(dbName)
	.setDbVersion(dbVersion)
    .setFactory(cursorFactory)
    .addEntity(Entity.class)
    .init();
```

If you need to execute some methods or logics before or after to create or update the data base, you can override the **onCreateDataBase, onDataBaseCreated, onUpdateDataBase and onDataBaseUpdated** methods.

```java
new EntityManager(this,"prueba",null,2){
    @Override
    public void onCreateDataBase(ConnectSQLite conn, SQLiteDatabase db) {
        super.onCreateDataBase(conn, db);
        Log.d("onCreateDatabase","testing the creation");
    }
}.addEntity(Users.class)
 .init();
```

##Tables

To define ours data **Tables** just need to extends the **Entity** class and use the annotations to provide the necessary information about the tables properties.

```java
//eager
@Table(AfterToCreated = {"setInsert"})
public class Writer extends Entity<Writer> {

    @Column(PrimaryKey = true,
            AutoIncrement = true)
    public int id;

    @Column(PrimaryKey = true)
    public String user;

    @Column(NotNull = true)
    public String email;

    @Column
    @OneToMany(ForeingKey = {"writer"})
    public List<WriteText> texts;

    private void setInsert(SQLiteDatabase db){}
}

@Table
public class Text extends Entity<Text> {

    @Column(NotNull = true,
            PrimaryKey = true,
            AutoIncrement = true)
    public int id;

    @Column(NotNull = true)
    public String text;

    @Column
    @OneToMany(ForeingKey = {"text"})
    public List<WriteText> writers;

}

@Table(Name = "RelationshipWriterText")
public class WriterText extends Entity<WriterText> {

    @Column(NotNull = true)
    @ManyToOne(ForeingKey = {"id"})
    public Writer writer;

    @Column(NotNull = true)
    @ManyToOne(ForeingKey = {"id"})
    public Text text;
}

//Lazy
@Table(Name = "RelationshipWriterText")
public class WriterText extends Entity<WriterText> {

    @Column(NotNull = true)
    @ManyToOne(ForeingKey = {"id"})
    public int writer;

    @Column(NotNull = true)
    @ManyToOne(ForeingKey = {"id"})
    public int text;
    
    public Text getWriter(){
    	return new Writer.fineOne(new EntityFilter().addArgument("id",writer+""));
    }

    public Text getText(){
    	return new Text.fineOne(new EntityFilter().addArgument("id",text+""));
    }
}
```

##Filter

You can use a simples **WHERE** conditions to find yours persistences.

```java
Writer writer = new Writer();
EntityFilter filter = new EntityFilter("?")
filter.addArgument("id","1",null,"and")
	.addArgument("user",null,"is not null","and")
    .addArgument("user","E%","like");
writer.findOnce(filter);
```

##Annotations

####1. **@Table**
|**Attributes**|**Default Value**|
|----------|-------------|
| Name     | 	""       |
|Synchronazable| true	 |
| NickName |	""		 |
| BeforeToCreate | {}	 |
|AfterToCreated | {}	 |

####2. @Column
|**Attributes**|**Default Value**|
|----------|-------------|
|	Name   | 	""		 |
|PrimaryKey|	false	 |
|AutoIncrement|	false	 |
|	NotNull|	false	 |
|DateFormat|"yyyy-MM-dd'T'HH:mm:ss.SSSZ"|
|	Length |	0		 |

####3. **Relationship**
|	Names	|**Attributes**|**Default Value**|
|-----------|----------|-------|
|**@OneToOne**|	ForeingKey	|	null	|
|			|	Create		|	true	|
|			|				|			|
|**@OneToMany**|ForeingKey	|	null	|
|			||				|			|
|**@ManyToOne**|ForeingKey	|	null	|
