package com.example.sculptor;

import java.util.ArrayList;
import java.util.List;

public class Sort {

  public enum Direction {
    ASCENDING,

    DESCENDING;
  }

  public enum NullPrecedence {
    NULLS_FIRST,

    NULLS_LAST;
  }

  public static class Column {
    private String name;
    private Direction direction;
    private NullPrecedence nullPrecedence;

    public Column(String name) {
      this(name, Direction.ASCENDING);
    }

    public Column(String name, Direction direction) {
      this.name = name;
      this.direction = direction;
    }

    public Column(String name, Direction direction, NullPrecedence nullPrecedence) {
      this.name = name;
      this.direction = direction;
      this.nullPrecedence = nullPrecedence;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public Direction getDirection() {
      return direction;
    }

    public void setDirection(Direction direction) {
      this.direction = direction;
    }

    public NullPrecedence getNullPrecedence() {
      return nullPrecedence;
    }

    public void setNullPrecedence(NullPrecedence nullPrecedence) {
      this.nullPrecedence = nullPrecedence;
    }
  }

  private List<Column> columns = new ArrayList<>();
  private boolean escapingEnabled = true;

  private Sort() {}

  public static Sort by(String column) {
    return new Sort().and(column);
  }

  public static Sort by(String column, Direction direction) {
    return new Sort().and(column, direction);
  }

  public static Sort by(String column, NullPrecedence nullPrecedence) {
    return by(column, Direction.ASCENDING, nullPrecedence);
  }

  public static Sort by(String column, Direction direction, NullPrecedence nullPrecedence) {
    return new Sort().and(column, direction, nullPrecedence);
  }

  public static Sort by(String... columns) {
    Sort sort = new Sort();
    for (String column : columns) {
      sort.and(column);
    }
    return sort;
  }

  public static Sort ascending(String... columns) {
    return by(columns);
  }

  public static Sort descending(String... columns) {
    Sort sort = new Sort();
    for (String column : columns) {
      sort.and(column, Direction.DESCENDING);
    }
    return sort;
  }

  public Sort descending() {
    return direction(Direction.DESCENDING);
  }

  public Sort ascending() {
    return direction(Direction.ASCENDING);
  }

  public Sort direction(Direction direction) {
    for (Column column : columns) {
      column.direction = direction;
    }
    return this;
  }

  public Sort and(String name) {
    columns.add(new Column(name));
    return this;
  }

  public Sort and(String name, Direction direction) {
    columns.add(new Column(name, direction));
    return this;
  }

  public Sort and(String name, NullPrecedence nullPrecedence) {
    return and(name, Direction.ASCENDING, nullPrecedence);
  }

  public Sort and(String name, Direction direction, NullPrecedence nullPrecedence) {
    columns.add(new Column(name, direction, nullPrecedence));
    return this;
  }

  public Sort disableEscaping() {
    escapingEnabled = false;
    return this;
  }

  public List<Column> getColumns() {
    return columns;
  }

  public static Sort empty() {
    return by();
  }

  public boolean isEscapingEnabled() {
    return escapingEnabled;
  }
}
