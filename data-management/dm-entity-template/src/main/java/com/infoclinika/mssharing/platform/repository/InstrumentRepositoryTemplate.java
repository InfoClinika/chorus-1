package com.infoclinika.mssharing.platform.repository;

import com.infoclinika.mssharing.platform.entity.InstrumentTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import static com.infoclinika.mssharing.platform.repository.QueryTemplates.IS_FILE_AVAILABLE;

/**
 * @author : Alexander Serebriyan, Herman Zamula
 */
public interface InstrumentRepositoryTemplate<T extends InstrumentTemplate> extends JpaRepository<T, Long> {
    String CLOSE_BRACKET = ")";

    String SELECT_INSTRUMENT_WITH_OPERATORS_BY_ID_USER = "select i from #{#entityName} i join i.lab l where " +
        " l.id in (select ulms.lab.id from UserLabMembership ulms where ulms.user.id = :user )";

    String SELECT_AVAILABLE_FILES =
        "(select instr.id from FileMetaDataTemplate f join f.instrument instr where f.isDeleted = false " +
            " and f.contentId is not null and " + IS_FILE_AVAILABLE + CLOSE_BRACKET;

    String IS_INSTRUMENT_AVAILABLE = " ( lm.user.id=:user or i.id in " + SELECT_AVAILABLE_FILES + " )";

    String IS_NAME_LIKE = " i.name like :s";
    String IS_LAB_MEMBER = " lm.user.id=:user ";

    String ACCESSED_SELECT = "select distinct " +
        "new com.infoclinika.mssharing.platform.repository.InstrumentRepositoryTemplate$AccessedInstrument(" +
        " i" +
        ", cast(:user as integer )" +
        ", i.name " +
        ", i.lab.name as labName " +
        ", i.serialNumber as serialNumber " +
        ", i.model.name as model " +
        CLOSE_BRACKET +
        " from #{#entityName} i ";

    String SELECT_COUNT = "select count(i.id) from InstrumentTemplate i ";

    String JOIN_LAB_MEMBERS = " join i.lab l left join l.labMemberships lm ";

    String ALL_AVAILABLE_INSTRUMENTS_ACCESSED_WITH_QUERY = ACCESSED_SELECT +
        " where " + QueryTemplates.Search.Instrument.FILTER_INSTRUMENTS_BY_QUERY +
        " and ( i.lab.id in (select l.id from LabTemplate l left join l.labMemberships lm " +
        "where lm.user.id = :user))";

    @Query("select i from #{#entityName} i " +
        " join i.lab l where l.id = :labId")
    List<T> findByLab(@Param("labId") long labId);

    @Query("select i from #{#entityName} i where i.lab.id = :lab and i.name like :s")
    Page<T> findByLab(@Param("lab") long lab, Pageable request, @Param("s") String nameFilter);

    /**
     * Pageable
     */
    @Query("select i from #{#entityName} i where i.lab in (select l.lab from UserTemplate u join u.labMemberships l " +
        "where u.id=:user) and i.name like :s")
    Page<T> findAllAvailable(@Param("user") long user, Pageable request, @Param("s") String nameFilter);

    @Query("select i from #{#entityName} i where i.lab in (select l.lab from UserTemplate u join u.labMemberships l " +
        "where u.id=:user)  or i.id in " + SELECT_AVAILABLE_FILES)
    List<T> findAllAvailable(@Param("user") long user);

    @Query("select count(i) from #{#entityName} i where i.model.id = :instrumentModel")
    long countByInstrumentModel(@Param("instrumentModel") long instrumentModel);

    @Query("select i from #{#entityName} i where i.serialNumber = :sn")
    T findBySerialNumber(@Param("sn") String serialNumber);

    @Query("select i from #{#entityName} i where i.name = :name and i.lab.id = :lab")
    T findOneByName(@Param("name") String name, @Param("lab") long lab);

    @Query("select distinct new com.infoclinika.mssharing.platform.repository.DictionaryRepoItem(" +
        "m.id, concat(v.name, ' - ', m.name) as name)" +
        " from #{#entityName} i join i.lab l join i.model m join m.vendor v " +
        " where (i in (" + SELECT_INSTRUMENT_WITH_OPERATORS_BY_ID_USER + ") or " +
        " i.id in " + SELECT_AVAILABLE_FILES + ") " +
        " and (cast(:lab as integer) = 0 or l.id = :lab) order by name")
    List<DictionaryRepoItem> availableInstrumentModels(@Param("user") long userId, @Param("lab") long labId);


    @Query("select distinct new com.infoclinika.mssharing.platform.repository.DictionaryRepoItem(" +
        "m.id, concat(v.name, ' - ', m.name) as name)" +
        " from #{#entityName} i join i.lab l join i.model m join m.vendor v " +
        " where (i in (" + SELECT_INSTRUMENT_WITH_OPERATORS_BY_ID_USER + ") or " +
        " i.id in " + SELECT_AVAILABLE_FILES + ") " +
        " and (cast(:lab as integer) = 0 or l.id = :lab) and m.studyType.id = :technologyType and m.vendor.id = " +
        ":vendor order by name")
    List<DictionaryRepoItem> availableInstrumentModels(@Param("user") long userId,
                                                       @Param("lab") long labId,
                                                       @Param("technologyType") long technologyType,
                                                       @Param("vendor") long vendor);

    @Query("select distinct new com.infoclinika.mssharing.platform.repository.DictionaryRepoItem(" +
        "m.id, concat(v.name, ' - ', m.name) as name)" +
        " from #{#entityName} i join i.lab l join i.model m join m.vendor v " +
        " where (i in (" + SELECT_INSTRUMENT_WITH_OPERATORS_BY_ID_USER + ") or " +
        " i.id in " + SELECT_AVAILABLE_FILES + ") " +
        " and (cast(:lab as integer) = 0 or l.id = :lab) and m.studyType.id = :technologyType and m.vendor.id = " +
        ":vendor " +
        "and m.type.id = :instrumentType order by name")
    List<DictionaryRepoItem> availableInstrumentModels(@Param("user") long userId,
                                                       @Param("lab") long labId,
                                                       @Param("technologyType") long technologyType,
                                                       @Param("vendor") long vendor,
                                                       @Param("instrumentType") long instrumentType);

    @Query("select i from #{#entityName} i join i.lab l join i.model m " +
        "where (i in (" + SELECT_INSTRUMENT_WITH_OPERATORS_BY_ID_USER + ") or " +
        "i.id in " + SELECT_AVAILABLE_FILES + ") and m.id = :model order by i.name")
    List<T> availableInstrumentsByModel(@Param("user") long userId, @Param("model") long modelId);

    /* AccessedInstrument methods */

    @Query(ACCESSED_SELECT + " where i.id=:id")
    AccessedInstrument<T> findOneAccessed(@Param("user") long user, @Param("id") long id);

    @Query(ACCESSED_SELECT + " join i.lab l where l.id in (select ulms.lab.id from UserLabMembership ulms " +
        "where ulms.user.id = :user ))")
    List<AccessedInstrument<T>> findAccessedWhereOperatorIs(@Param("user") long user);

    @Query(ACCESSED_SELECT + JOIN_LAB_MEMBERS + " where " + IS_LAB_MEMBER)
    List<AccessedInstrument<T>> findAllAvailableAccessed(@Param("user") long user);

    @Query(value = ACCESSED_SELECT + JOIN_LAB_MEMBERS + " where " + IS_LAB_MEMBER + " and " + IS_NAME_LIKE,
        countQuery = SELECT_COUNT + JOIN_LAB_MEMBERS + " where " + IS_LAB_MEMBER + " and " + IS_NAME_LIKE)
    Page<AccessedInstrument<T>> findAllAvailableAccessed(@Param("user") long user, @Param("s") String nameFilter,
                                                         Pageable request);

    @Query(ACCESSED_SELECT + JOIN_LAB_MEMBERS + " where l.id = :lab  and " + IS_INSTRUMENT_AVAILABLE)
    List<AccessedInstrument<T>> findByLabAccessed(@Param("user") long user, @Param("lab") long labId);

    @Query(
        value = ACCESSED_SELECT + JOIN_LAB_MEMBERS +
            " where i.lab.id = :lab and " + IS_NAME_LIKE + " and " + IS_INSTRUMENT_AVAILABLE,
        countQuery = SELECT_COUNT + JOIN_LAB_MEMBERS +
            " where i.lab.id = :lab and " + IS_NAME_LIKE + " and " + IS_INSTRUMENT_AVAILABLE
    )
    Page<AccessedInstrument<T>> findByLabAccessed(@Param("user") long user, @Param("lab") long lab, Pageable request,
                                                  @Param("s") String nameFilter);

    @Query(ACCESSED_SELECT + JOIN_LAB_MEMBERS +
        " where l.id = :lab and i.model.id = :model and i.name = :name  and " + IS_INSTRUMENT_AVAILABLE)
    AccessedInstrument<T> findByLabModelAndNameAccessed(@Param("user") long user, @Param("lab") long labId,
                                                        @Param("model") long model, @Param("name") String name);

    @Query(ACCESSED_SELECT + JOIN_LAB_MEMBERS +
        " where l.id = :lab and i.model.studyType.id = :studyType and " + IS_INSTRUMENT_AVAILABLE)
    List<AccessedInstrument<T>> findByLabAndStudyTypeAccessed(@Param("user") long user, @Param("lab") long labId,
                                                              @Param("studyType") long studyTypeId);


    /* For Search purposes */

    @Query(QueryTemplates.Search.Instrument.COUNT_ALL_AVAILABLE_INSTRUMENTS_WITH_QUERY)
    long searchInstrumentsCount(@Param("user") long user, @Param("query") String query);

    @Query(QueryTemplates.Search.Instrument.ALL_AVAILABLE_INSTRUMENTS_WITH_QUERY)
    List<T> searchInstruments(@Param("user") long user, @Param("query") String query);

    @Query(QueryTemplates.Search.Instrument.ALL_AVAILABLE_INSTRUMENTS_WITH_QUERY)
    Page<T> searchPagedInstruments(@Param("user") long user, @Param("query") String query, Pageable pageable);

    @Query(ALL_AVAILABLE_INSTRUMENTS_ACCESSED_WITH_QUERY)
    List<AccessedInstrument<T>> searchInstrumentsAccessed(@Param("user") long user, @Param("query") String query);

    @Query(
        value = ALL_AVAILABLE_INSTRUMENTS_ACCESSED_WITH_QUERY,
        countQuery = QueryTemplates.Search.Instrument.COUNT_ALL_AVAILABLE_INSTRUMENTS_WITH_QUERY
    )
    Page<AccessedInstrument<T>> searchPagedInstrumentsAccessed(@Param("user") long user, @Param("query") String query,
                                                               Pageable pageable);

    @Query(
        "select distinct new com.infoclinika.mssharing.platform.repository.DictionaryRepoItem(t.id, t.name as name)" +
            " from #{#entityName} i join i.lab l join i.model m join m.vendor v join m.type t" +
            " where (cast(:lab as integer) = 0 or l.id = :lab) and m.studyType.id = :technologyType " +
            " and m.vendor.id = :vendor order by name")
    List<DictionaryRepoItem> availableInstrumentTypes(@Param("lab") long labId,
                                                      @Param("technologyType") long technologyType,
                                                      @Param("vendor") long vendor);


    class AccessedInstrument<I extends InstrumentTemplate> {

        public final I instrument;
        public final long accessedUser;
        public final SortingFieldsData sortingFieldsData;

        public AccessedInstrument(I instrument,
                                  Integer accessedUser,
                                  String name,
                                  String labName,
                                  String serialNumber,
                                  String model) {
            this.instrument = instrument;
            this.accessedUser = accessedUser.longValue();
            this.sortingFieldsData = new SortingFieldsData(instrument.getId(), name, labName, serialNumber, model);
        }

        public static class SortingFieldsData {
            public final Long id;
            public final String name;
            public final String labName;
            public final String serialNumber;
            public final String model;

            public SortingFieldsData(Long id, String name, String labName, String serialNumber, String model) {
                this.id = id;
                this.name = name;
                this.labName = labName;
                this.serialNumber = serialNumber;
                this.model = model;
            }
        }
    }
}
