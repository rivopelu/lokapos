package com.lokapos.services;

import com.lokapos.model.request.RequestCreateEditCategory;
import com.lokapos.model.request.RequestCreateEditMenu;
import com.lokapos.model.response.ResponseCategoryList;
import com.lokapos.model.response.ResponseDetailMenu;
import com.lokapos.model.response.ResponseListMenu;

import java.util.List;

public interface MasterDataService {

    String createNewCategory(List<RequestCreateEditCategory> req);

    List<ResponseCategoryList> getAllCategories();

    String createNewMenu(RequestCreateEditMenu req);

    List<ResponseListMenu> getAllMenus();

    String editMenu(RequestCreateEditMenu req, String id);

    ResponseDetailMenu getDetailMenu(String id);
}
