package com.softmotions.ncms.asm;

import java.io.Serializable;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.AbstractIterator;
import com.google.common.util.concurrent.Striped;
import com.softmotions.commons.cont.AbstractIndexedCollection;
import com.softmotions.commons.cont.KVOptions;
import com.softmotions.commons.cont.Pair;

/**
 * Assembly.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@NotThreadSafe
@SuppressWarnings("unchecked")
@JsonRootName("asm")
@XmlAccessorType(XmlAccessType.NONE)
@JsonAutoDetect(
        creatorVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE
)
public class Asm implements Serializable {

    // 255 different locks to access all asms
    public static final Striped<Lock> STRIPED_LOCKS = Striped.lazyWeakLock(255);

    interface ViewFull {
    }

    interface ViewLarge extends ViewFull {

    }

    @JsonProperty
    Long id;

    @JsonProperty
    String name;

    @JsonProperty
    String hname;

    @JsonProperty
    String type;

    @JsonProperty
    String description;

    @JsonProperty
    AsmCore core;

    @JsonProperty
    String controller;

    @JsonProperty
    String options;

    @JsonProperty
    boolean template;

    @JsonProperty
    boolean published;

    @JsonProperty
    boolean shadowed;

    @JsonProperty
    String templateMode;

    @JsonProperty
    String lockUser;

    Date cdate;

    Date mdate;

    Date edate;

    KVOptions parsedOptions;

    List<Asm> parents;

    AttrsList attributes;

    Map<String, AsmAttribute> type2uniqueAttrCache;

    Long navParentId;

    String navAlias;

    String navAlias2;

    String navCachedPath;

    /**
     * Default language of asm content data
     */
    String lang;

    Collection<String> accessRoles;

    Date lockDate;


    public Asm() {
    }

    public Asm(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Asm(String name) {
        this.name = name;
    }

    public Asm(String name, AsmCore core) {
        this.name = name;
        this.core = core;
    }

    public Asm(String name, AsmCore core, String description, String options) {
        this.name = name;
        this.core = core;
        this.description = description;
        this.options = options;
    }

    public static Lock acquireLock(Long asmId) {
        Lock lock = STRIPED_LOCKS.get(asmId);
        boolean acquired;
        try {
            acquired = lock.tryLock(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException("Failed to acquire assembly lock. Assembly id: " + asmId, e);
        }
        if (!acquired) {
            throw new RuntimeException("Failed to acquire assembly lock. Assembly id: " + asmId);
        }
        return lock;
    }

    /**
     * Assembly primary key (PK)
     */
    @Nonnull
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Assembly name.
     * If assembly represents a page its name
     * will be `UUID`.
     */
    @Nonnull
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Assembly human name.
     * It is page name.
     */
    @Nullable
    public String getHname() {
        return hname;
    }

    public void setHname(String hname) {
        this.hname = hname;
    }

    /**
     * An assembly type.
     * <p/>
     * Valid values:
     * <p/>
     * * `null` - generic assembly
     * * `page` - page instance
     * * `page.folder` - page instance with specific `folder` flavour.
     * * `new.page` - Special `news` page instance.
     */
    @Nonnull
    public String getType() {
        return type != null ? type : "";
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * Assembly description
     */
    @Nullable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Internal assembly options serialized in string
     * in the same format as the
     * {@link KVOptions#toString()} does
     */
    @Nullable
    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
        this.parsedOptions = null;
    }

    /**
     * Internal assembly options as key value map.
     */
    @Nonnull
    public KVOptions getParsedOptions() {
        String opts = this.options;
        if (opts == null) {
            return new KVOptions();
        }
        this.parsedOptions = new KVOptions(opts);
        return parsedOptions;
    }

    /**
     * Returns `true` if this assembly acts as template
     * for site pages.
     */
    public boolean isTemplate() {
        return template;
    }

    public void setTemplate(boolean template) {
        this.template = template;
    }

    /**
     * Return `true` if this assembly is a shadow of another assembly
     */
    public boolean isShadowed() {
        return shadowed;
    }

    public void setShadowed(boolean shadowed) {
        this.shadowed = shadowed;
    }

    /**
     * The type of template represented by this assembly.
     * Valid values:
     * <p/>
     * * `null`/`none` - assembly is not a template
     * * `page` - assembly is a page template
     * * `news` - assembky is a template form news line
     */
    @Nullable
    public String getTemplateMode() {
        return templateMode;
    }

    public void setTemplateMode(String templateMode) {
        this.templateMode = templateMode;
    }

    /**
     * Modification date
     */
    @Nullable
    public Date getMdate() {
        return mdate;
    }

    public void setMdate(Date mdate) {
        this.mdate = mdate;
    }

    /**
     * Assembly creation date.
     */
    @Nullable
    public Date getCdate() {
        return cdate;
    }

    public void setCdate(Date cdate) {
        this.cdate = cdate;
    }

    /**
     * Assembly end date. TODO description.
     */
    @Nullable
    public Date getEdate() {
        return edate;
    }

    public void setEdate(Date edate) {
        this.edate = edate;
    }

    /**
     * User which acquired lock on this assembly.
     */
    public String getLockUser() {
        return lockUser;
    }

    public void setLockUser(String lockUser) {
        this.lockUser = lockUser;
    }

    /**
     * Assembly locking date.
     */
    public Date getLockDate() {
        return lockDate;
    }

    public void setLockDate(Date lockDate) {
        this.lockDate = lockDate;
    }

    /**
     * `True` if assembly is published.
     */
    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    @Nullable
    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    /**
     * Class name of an optional assembly controller.
     * *
     */
    @Nullable
    public String getController() {
        return controller;
    }

    public void setController(String controller) {
        this.controller = controller;
    }

    /**
     * Class name of an optional assembly controller
     * stored in this assembly or in any of its parents.
     */
    @Nullable
    @JsonProperty()
    public String getEffectiveController() {
        String c = getController();
        if (c != null || getParents() == null) {
            return c;
        }
        for (final Asm p : getParents()) {
            c = p.getEffectiveController();
            if (c != null) {
                return c;
            }
        }
        return null;
    }

    /**
     * Assembly core instance.
     */
    @Nullable
    public AsmCore getCore() {
        return core;
    }

    public void setCore(@Nullable AsmCore core) {
        this.core = core;
    }

    /**
     * Assembly core instance
     * stored in this assembly or in any of its parents.
     */
    @Nullable
    @JsonProperty
    public AsmCore getEffectiveCore() {
        AsmCore c = getCore();
        if (c != null || getParents() == null) {
            return c;
        }
        for (final Asm p : getParents()) {
            c = p.getEffectiveCore();
            if (c != null) {
                return c;
            }
        }
        return null;
    }

    /**
     * Roles which can use this assembly.
     */
    @JsonView(Asm.ViewFull.class)
    @Nullable
    public Collection<String> getAccessRoles() {
        return accessRoles;
    }

    /**
     * List of all direct assembly parents.
     */
    @Nullable
    public List<Asm> getParents() {
        return parents;
    }

    public void setParents(List<Asm> parents) {
        this.parents = parents;
    }

    /**
     * ID of assembly which parent of
     * this assembly in the site's navigation structure.
     */
    @Nullable
    public Long getNavParentId() {
        return navParentId;
    }

    public void setNavParentId(Long navParentId) {
        this.navParentId = navParentId;
    }

    /**
     * Assembly navigation alias.
     */
    @Nullable
    public String getNavAlias() {
        return navAlias;
    }

    /**
     * Alternate assembly navigation alias.
     */
    @Nullable
    public String getNavAlias2() {
        return navAlias2;
    }

    public void setNavAlias(String navAlias) {
        this.navAlias = navAlias;
    }

    /**
     * String represents the current navigation
     * position of this assembly. It it cached value
     * in order perform fast SQL navigation queries.
     */
    @Nullable
    public String getNavCachedPath() {
        return navCachedPath;
    }

    public void setNavCachedPath(String navCachedPath) {
        this.navCachedPath = navCachedPath;
    }

    /**
     * Iterator for all assembly parents, direct or indirect.
     */
    @Nonnull
    public Iterator<Asm> getAllParentsIterator() {
        List<Pair<Asm, Integer>> plist = new ArrayList<>();
        fetchParentsCumulative(plist, 0);
        Collections.sort(plist, (o1, o2) -> {
            int res = Integer.compare(o1.getTwo(), o2.getTwo());
            if (res == 0) {
                Collator coll = Collator.getInstance();
                res = coll.compare(o1.getOne().getName(), o2.getOne().getName());
            }
            return res;
        });
        final Iterator<Pair<Asm, Integer>> pit = plist.iterator();
        return new AbstractIterator<Asm>() {
            @Override
            protected Asm computeNext() {
                if (pit.hasNext()) {
                    return pit.next().getOne();
                }
                return endOfData();
            }
        };
    }

    private void fetchParentsCumulative(List<Pair<Asm, Integer>> pcont, Integer level) {
        List<Asm> plist = getParents();
        if (plist == null || plist.isEmpty()) {
            return;
        }
        for (final Asm p : getParents()) {
            pcont.add(new Pair<>(p, level));
            p.fetchParentsCumulative(pcont, level + 1);
        }
    }

    /**
     * Set contains all assembly parents direct or indirect.
     */
    @Nonnull
    public Set<String> getAllParentNames() {
        List<Asm> plist = getParents();
        if (plist == null || plist.isEmpty()) {
            return Collections.EMPTY_SET;
        }
        Set<String> cparents = new HashSet<>();
        for (final Asm p : getParents()) {
            cparents.add(p.getName());
            cparents.addAll(p.getAllParentNames());
        }
        return cparents;

    }

    /**
     * Get page refs for all direct assembly parents.
     */
    @JsonProperty()
    @Nonnull
    public String[] getParentRefs() {
        List<Asm> plist = getParents();
        if (plist == null || plist.isEmpty()) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        String[] prefs = new String[plist.size()];
        for (int i = 0; i < prefs.length; ++i) {
            Asm p = parents.get(i);
            prefs[i] = p.getId() + ":" + p.getName();
        }
        return prefs;
    }

    /**
     * Get attribute instance
     *
     * @param name Attribute name
     * @return Attribute instance if attribute with specified `name`
     * exists in this assembly or its parents. Or `null`
     * if attribute with this name not found.
     */
    @Nullable
    public AsmAttribute getEffectiveAttribute(String name) {
        AsmAttribute attr = getAttribute(name);
        if (attr != null || getParents() == null) {
            return attr;
        }
        for (final Asm p : getParents()) {
            attr = p.getEffectiveAttribute(name);
            if (attr != null) {
                return attr;
            }
        }
        return null;
    }

    @Nullable
    public AsmAttribute getUniqueEffectiveAttributeByType(String attrType) {
        AsmAttribute attr = getUniqueAttributeByType(attrType);
        if (attr != null || getParents() == null) {
            return attr;
        }
        for (final Asm p : getParents()) {
            attr = p.getUniqueEffectiveAttributeByType(attrType);
            if (attr != null) {
                return attr;
            }
        }
        return null;
    }

    @Nullable
    public AsmAttribute getUniqueAttributeByType(String attrType) {
        AsmAttribute ret = (type2uniqueAttrCache != null) ? type2uniqueAttrCache.get(attrType) : null;
        if (ret != null) {
            return ret;
        }
        if (getAttributes() == null) {
            return null;
        }
        for (AsmAttribute attr : getAttributes()) {
            if (attrType.equals(attr.getType())) {
                if (type2uniqueAttrCache == null) {
                    type2uniqueAttrCache = new HashMap<>(4);
                }
                type2uniqueAttrCache.put(attrType, attr);
                return attr;
            }
        }
        return null;
    }

    /**
     * Return `true` if assembly has attribute with specified `name`
     * or in any of its parents.
     */
    public boolean isHasAttribute(String name) {
        return (getEffectiveAttribute(name) != null);
    }

    @Nullable
    public String getEffectiveAttributeAsString(String name, String defVal) {
        AsmAttribute attr = getEffectiveAttribute(name);
        if (attr == null) {
            return defVal;
        }
        return attr.getEffectiveValue();
    }

    @Nonnull
    public String[] getEffectiveAttributeAsStringArray(String name, ObjectMapper mapper) {
        AsmAttribute attr = getEffectiveAttribute(name);
        if (attr == null) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        String val = attr.getEffectiveValue();
        if (StringUtils.isBlank(val)) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        val = val.trim();
        if (val.length() > 1 && val.charAt(0) == '[' && val.charAt(val.length() - 1) == ']') { //todo Naive JSON array detection :(
            try {
                ArrayNode an = (ArrayNode) mapper.readTree(val);
                String[] ret = new String[an.size()];
                for (int i = 0, l = ret.length; i < l; ++i) {
                    ret[i] = an.get(i).asText();
                }
                return ret;
            } catch (Exception ignored) {
                return ArrayUtils.EMPTY_STRING_ARRAY;
            }
        } else {
            return com.softmotions.commons.cont.ArrayUtils.split(val, " ,;");
        }
    }

    /**
     * Return instance of direct assembly attribute if found.
     *
     * @param name
     * @return Found assembly attribute instance or `null`
     */
    @Nullable
    public AsmAttribute getAttribute(String name) {
        return (getAttributes() != null) ? attributes.getIndex().get(name) : null;
    }

    /**
     * List of all direct assembly attributes
     */
    @Nullable
    public Collection<AsmAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(AttrsList attributes) {
        this.attributes = attributes;
        if (type2uniqueAttrCache != null) {
            type2uniqueAttrCache.clear();
        }
    }

    /**
     * Register a new attribute in this assembly.
     */
    public void addAttribute(AsmAttribute attr) {
        if (getAttributes() == null) {
            attributes = new AttrsList();
        }
        attributes.add(attr);
    }

    public void rmAttribute(String name) {
        if (getAttributes() == null || attributes.isEmpty()) {
            return;
        }
        AsmAttribute attr = attributes.getIndex().get(name);
        if (attr != null) {
            attributes.remove(attr);
        }
    }

    @Nonnull
    public Collection<String> getAttributeNames() {
        if (getAttributes() == null) {
            return Collections.EMPTY_LIST;
        }
        List<String> anames = new ArrayList<>(getAttributes().size());
        for (final AsmAttribute a : attributes) {
            anames.add(a.getName());
        }
        return anames;
    }


    @Nonnull
    public Collection<String> getEffectiveAttributeNames() {
        if (getAttributes() == null) {
            return Collections.EMPTY_LIST;
        }
        final Set<String> anames = new HashSet<>(getAttributes().size() << 1);
        if (attributes != null) {
            for (final AsmAttribute a : attributes) {
                anames.add(a.getName());
            }
        }
        if (getParents() != null) {
            for (final Asm p : getParents()) {
                anames.addAll(p.getEffectiveAttributeNames());
            }
        }
        return anames;
    }

    @JsonProperty
    public Collection<AsmAttribute> getEffectiveAttributes() {
        Collection<AsmAttribute> attrs = getAttributes();
        ArrayList<AsmAttribute> res =
                new ArrayList<>(attrs != null && attrs.size() > 10 ?
                                attrs.size() << 1 : 10);
        addSortedChainAttributes(res, this);
        Map<String, Integer> pmap = new HashMap<>();
        for (int i = 0; i < res.size(); ++i) {
            AsmAttribute a = res.get(i);
            Integer pind = pmap.get(a.getName());
            if (pind != null) {
                a.setOverriddenParent(res.get(pind));
                res.set(pind, a);
                res.remove(i);
                --i;
            } else {
                pmap.put(a.getName(), i);
            }
        }
        return res;
    }

    private void addSortedChainAttributes(ArrayList<AsmAttribute> res, Asm asm) {
        List<AsmAttribute> slist = asm.getSortedLocalAttributes();
        res.addAll(0, slist);
        if (asm.getParents() != null) {
            for (final Asm p : asm.getParents()) {
                addSortedChainAttributes(res, p);
            }
        }
    }

    @Nonnull
    private List<AsmAttribute> getSortedLocalAttributes() {
        if (getAttributes() == null) {
            return Collections.EMPTY_LIST;
        }
        List<AsmAttribute> local = new ArrayList<>(getAttributes().size());
        local.addAll(getAttributes());
        Collections.sort(local);
        return local;
    }

    public static class AttrsList extends AbstractIndexedCollection<String, AsmAttribute> implements Serializable {

        @Override
        protected String getElementKey(AsmAttribute el) {
            return el.getName();
        }

        public AttrsList() {
        }

        protected AttrsList(int size) {
            super(size);
        }

        public AttrsList cloneDeep() {
            AttrsList nlist = new AttrsList(size());
            for (AsmAttribute attr : this) {
                nlist.add(attr.cloneDeep());
            }
            return nlist;
        }
    }

    /**
     * Perform deep clone of this assembly.
     * Cloned parents are cached in <c>cloneContext</c>.
     */
    public Asm cloneDeep(Map<String, Asm> cloneContext) {
        Asm asm = cloneContext.get(name);
        if (asm != null) {
            return asm;
        }
        asm = new Asm();
        asm.id = id;
        asm.name = name;
        asm.hname = hname;
        asm.description = description;
        asm.options = options;
        asm.published = published;
        asm.template = template;
        asm.controller = controller;
        asm.core = (core != null) ? core.cloneDeep() : null;
        asm.cdate = cdate;
        asm.mdate = mdate;
        asm.edate = edate;
        asm.attributes = (attributes != null) ? attributes.cloneDeep() : null;
        asm.navParentId = navParentId;
        asm.navAlias = navAlias;
        asm.navAlias2 = navAlias2;
        asm.navCachedPath = navCachedPath;
        asm.lang = lang;
        asm.shadowed = shadowed;
        asm.lockUser = lockUser;
        asm.lockDate = lockDate;
        if (getParents() != null) {
            asm.parents = new ArrayList<>(getParents().size());
            for (Asm parent : getParents()) {
                asm.parents.add(parent.cloneDeep(cloneContext));
            }
        }
        cloneContext.put(asm.name, asm);
        return asm;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !Asm.class.isAssignableFrom(o.getClass())) {
            return false;
        }
        return Objects.equals(name, ((Asm) o).name);
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder("Asm{");
        sb.append("id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", hname='").append(hname).append('\'');
        sb.append(", options='").append(options).append('\'');
        sb.append(", core=").append(core);
        sb.append(", description='").append(description).append('\'');
        sb.append(", navParentId=").append(navParentId);
        sb.append(", attributes=").append(attributes);
        sb.append('}');
        return sb.toString();
    }
}
