package project.app.humanelogistics.preprocessing;

import project.app.humanelogistics.model.DamageCategory;

public interface ContentClassifier {
    DamageCategory classify(String text);
}