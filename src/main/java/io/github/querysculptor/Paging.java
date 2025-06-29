package io.github.querysculptor;

public class Paging {

  private final int index;

  private final int size;

  public Paging(int size) {
    this(0, size);
  }

  public Paging(int index, int size) {
    this.size = Math.max(size, -1);
    if (size == -1) {
      this.index = 0;
    } else {
      this.index = Math.max(index - 1, 0);
    }
  }

  public static Paging unPaged() {
    return new Paging(0, -1);
  }

  public static Paging of(int index, int size) {
    return new Paging(index, size);
  }

  public static Paging ofSize(int size) {
    return new Paging(size);
  }

  public boolean isPaged() {
    return size != -1;
  }

  public boolean isUnPaged() {
    return !isPaged();
  }

  public Paging next() {
    return new Paging(index + 1, size);
  }

  public Paging previous() {
    return index > 0 ? new Paging(index - 1, size) : this;
  }

  public Paging first() {
    return index > 0 ? new Paging(0, size) : this;
  }

  public Paging index(int newIndex) {
    return newIndex != index ? new Paging(newIndex, size) : this;
  }

  public int getIndex() {
    return index;
  }

  public int getSize() {
    return size;
  }
}
