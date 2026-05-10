public class Book implements Item {
    private final int id;
    private final String name;
    private final String author;
    private final int pages;

    /**
     * Constructs a Book from a tab-separated string with 4 fields:
     * <id> <tab> <name> <tab> <author> <tab> <pages>
     */
    public Book(String line) {
        String[] parts = line.split("\t");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid book line: " + line);
        }
        this.id = Integer.parseInt(parts[0].trim());
        this.name = parts[1].trim();
        this.author = parts[2].trim();
        this.pages = Integer.parseInt(parts[3].trim());
    }

    @Override
    public int getId() { return id; }

    @Override
    public String getName() { return name; }

    public String getAuthor() { return author; }

    public int getPages() { return pages; }

    @Override
    public String toString() {
        return "Book{id=%d, name=%s, author=%s, pages=%d}"
                .formatted(id, name, author, pages);
    }
}
