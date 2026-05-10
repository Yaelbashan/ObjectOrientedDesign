class Rating<T extends Item> {
    private final int userId;
    private final int itemId;
    private final double rating; // 1‑5
    public Rating(int userId, int itemId, double rating) {
        this.userId = userId; this.itemId = itemId; this.rating = rating; }
    public Rating(String line) {
        String[] parts = line.split("\t");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid rating line: " + line);
        }
        this.userId = Integer.parseInt(parts[0].trim());
        this.itemId = Integer.parseInt(parts[1].trim());
        this.rating = Integer.parseInt(parts[2].trim());
    }
    public int getUserId() { return userId; }
    public int getItemId() { return itemId; }
    public double getRating() { return rating; }
}
