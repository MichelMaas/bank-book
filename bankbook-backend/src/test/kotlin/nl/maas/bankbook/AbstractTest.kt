package nl.maas.bankbook

import nl.maas.bankbook.utils.CSVUtils

abstract class AbstractTest {
    protected val parsedFile = CSVUtils.parseFile("/home/michel/projects/tr-info_16242048_20220103091414.CSV");
}