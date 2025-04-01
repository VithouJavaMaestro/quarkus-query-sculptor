package com.example.sculptor;

public class PageRequest {

  private final Paging paging;

  private final Sort sort;

  public PageRequest(Paging paging, Sort sort) {
    this.paging = paging;
    this.sort = sort;
  }

  public PageRequest(Sort sort) {
    this.paging = Paging.unPaged();
    this.sort = sort;
  }

  public PageRequest(Paging paging) {
    this.paging = paging;
    this.sort = Sort.empty();
  }

  public Paging getPage() {
    return paging;
  }

  public Sort getSort() {
    return sort;
  }
}
