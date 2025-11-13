/**
 * 数据库配置
 * 包含所有支持的数据库类型、图标、颜色等信息
 */

export interface DatabaseConfig {
  value: string
  label: string
  icon: string  // SVG图标或图片URL
  color: string
  defaultPort: number
  category: 'RDBMS' | 'OLAP' | 'NoSQL' | 'Search' | 'TimeSeries'
}

export const databaseConfigs: DatabaseConfig[] = [
  // ========== RDBMS 关系型数据库 ==========
  {
    value: 'MySQL',
    label: 'MySQL',
    icon: 'https://www.mysql.com/common/logos/logo-mysql-170x115.png',
    color: '#00758F',
    defaultPort: 3306,
    category: 'RDBMS'
  },
  {
    value: 'Oracle',
    label: 'Oracle',
    icon: 'https://www.oracle.com/a/ocom/img/sql-dev.svg',
    color: '#F80000',
    defaultPort: 1521,
    category: 'RDBMS'
  },
  {
    value: 'SQLServer',
    label: 'SQL Server',
    icon: 'https://cdn.worldvectorlogo.com/logos/microsoft-sql-server-1.svg',
    color: '#CC2927',
    defaultPort: 1433,
    category: 'RDBMS'
  },
  {
    value: 'PostgreSQL',
    label: 'PostgreSQL',
    icon: 'https://www.postgresql.org/media/img/about/press/elephant.png',
    color: '#336791',
    defaultPort: 5432,
    category: 'RDBMS'
  },
  {
    value: 'DB2',
    label: 'IBM DB2',
    icon: 'https://www.ibm.com/brand/experience-guides/developer/b1db1ae501d522a1a4b49613fe07c9f1/01_8-bar-positive.svg',
    color: '#054ADA',
    defaultPort: 50000,
    category: 'RDBMS'
  },
  {
    value: 'Sybase',
    label: 'Sybase',
    icon: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNjQiIGhlaWdodD0iNjQiIHZpZXdCb3g9IjAgMCA2NCA2NCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iNjQiIGhlaWdodD0iNjQiIGZpbGw9IiMwMDc4RDQiLz48dGV4dCB4PSI1MCUiIHk9IjUwJSIgZm9udC1mYW1pbHk9IkFyaWFsIiBmb250LXNpemU9IjI0IiBmaWxsPSJ3aGl0ZSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9Ii4zZW0iPlM8L3RleHQ+PC9zdmc+',
    color: '#0078D4',
    defaultPort: 5000,
    category: 'RDBMS'
  },
  {
    value: 'SQLite',
    label: 'SQLite',
    icon: 'https://www.sqlite.org/images/sqlite370_banner.gif',
    color: '#003B57',
    defaultPort: 0,
    category: 'RDBMS'
  },
  {
    value: 'DM',
    label: '达梦数据库',
    icon: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNjQiIGhlaWdodD0iNjQiIHZpZXdCb3g9IjAgMCA2NCA2NCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iNjQiIGhlaWdodD0iNjQiIGZpbGw9IiMwMGE4NzAiLz48dGV4dCB4PSI1MCUiIHk9IjUwJSIgZm9udC1mYW1pbHk9IkFyaWFsIiBmb250LXNpemU9IjI0IiBmaWxsPSJ3aGl0ZSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9Ii4zZW0iPkRNPC90ZXh0Pjwvc3ZnPg==',
    color: '#00a870',
    defaultPort: 5236,
    category: 'RDBMS'
  },
  {
    value: 'KingBase',
    label: '人大金仓',
    icon: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNjQiIGhlaWdodD0iNjQiIHZpZXdCb3g9IjAgMCA2NCA2NCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iNjQiIGhlaWdodD0iNjQiIGZpbGw9IiMwNTk0ZmEiLz48dGV4dCB4PSI1MCUiIHk9IjUwJSIgZm9udC1mYW1pbHk9IkFyaWFsIiBmb250LXNpemU9IjIwIiBmaWxsPSJ3aGl0ZSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9Ii4zZW0iPktCPC90ZXh0Pjwvc3ZnPg==',
    color: '#0594fa',
    defaultPort: 54321,
    category: 'RDBMS'
  },

  // ========== OLAP 分析型数据库 ==========
  {
    value: 'Trino',
    label: 'Trino',
    icon: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNjQiIGhlaWdodD0iNjQiIHZpZXdCb3g9IjAgMCA2NCA2NCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iNjQiIGhlaWdodD0iNjQiIGZpbGw9IiNERDAwQTEiLz48dGV4dCB4PSI1MCUiIHk9IjUwJSIgZm9udC1mYW1pbHk9IkFyaWFsIiBmb250LXNpemU9IjIwIiBmaWxsPSJ3aGl0ZSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9Ii4zZW0iIGZvbnQtd2VpZ2h0PSJib2xkIj5Ucmluby90ZXh0Pjwvc3ZnPg==',
    color: '#DD00A1',
    defaultPort: 29090,
    category: 'OLAP'
  },
  {
    value: 'Hive',
    label: 'Apache Hive',
    icon: 'https://avatars.githubusercontent.com/u/47359?s=200&v=4',
    color: '#FDEE21',
    defaultPort: 10000,
    category: 'OLAP'
  },
  {
    value: 'ClickHouse',
    label: 'ClickHouse',
    icon: 'https://avatars.githubusercontent.com/u/54801242?s=200&v=4',
    color: '#FFCC00',
    defaultPort: 8123,
    category: 'OLAP'
  },
  {
    value: 'StarRocks',
    label: 'StarRocks',
    icon: 'https://avatars.githubusercontent.com/u/79315181?s=200&v=4',
    color: '#6366F1',
    defaultPort: 9030,
    category: 'OLAP'
  },
  {
    value: 'Doris',
    label: 'Apache Doris',
    icon: 'https://avatars.githubusercontent.com/u/44544266?s=200&v=4',
    color: '#4C8BF5',
    defaultPort: 9030,
    category: 'OLAP'
  },
  {
    value: 'GreenPlum',
    label: 'GreenPlum',
    icon: 'https://avatars.githubusercontent.com/u/16605186?s=200&v=4',
    color: '#6AB023',
    defaultPort: 5432,
    category: 'OLAP'
  },

  // ========== NoSQL 数据库 ==========
  {
    value: 'MongoDB',
    label: 'MongoDB',
    icon: 'https://avatars.githubusercontent.com/u/45120?s=200&v=4',
    color: '#47A248',
    defaultPort: 27017,
    category: 'NoSQL'
  },
  {
    value: 'Redis',
    label: 'Redis',
    icon: 'https://avatars.githubusercontent.com/u/1529926?s=200&v=4',
    color: '#DC382D',
    defaultPort: 6379,
    category: 'NoSQL'
  },

  // ========== 搜索引擎 ==========
  {
    value: 'ElasticSearch',
    label: 'ElasticSearch',
    icon: 'https://avatars.githubusercontent.com/u/6764390?s=200&v=4',
    color: '#00BFB3',
    defaultPort: 9200,
    category: 'Search'
  },

  // ========== 时序数据库 ==========
  {
    value: 'TDEngine',
    label: 'TDEngine',
    icon: 'https://avatars.githubusercontent.com/u/39627677?s=200&v=4',
    color: '#00A6A0',
    defaultPort: 6030,
    category: 'TimeSeries'
  }
]

/**
 * 根据数据库类型获取配置
 */
export const getDatabaseConfig = (type: string): DatabaseConfig | undefined => {
  return databaseConfigs.find(db => db.value === type)
}

/**
 * 根据分类获取数据库列表
 */
export const getDatabasesByCategory = (category: string): DatabaseConfig[] => {
  return databaseConfigs.filter(db => db.category === category)
}

/**
 * 获取所有数据库分类
 */
export const databaseCategories = [
  { value: 'RDBMS', label: '关系型数据库' },
  { value: 'OLAP', label: '分析型数据库' },
  { value: 'NoSQL', label: 'NoSQL数据库' },
  { value: 'Search', label: '搜索引擎' },
  { value: 'TimeSeries', label: '时序数据库' }
]

