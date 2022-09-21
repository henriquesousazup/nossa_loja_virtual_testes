package br.com.zup.edu.nossalojavirtual.products;

import java.util.List;

public interface PhotoUploader {

    List<Photo> upload(List<String> photos, PreProduct preProduct);
}
