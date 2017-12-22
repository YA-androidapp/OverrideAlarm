package jp.gr.java_conf.ya.overridealarm; // Copyright (c) 2017 YA <ya.androidapp@gmail.com> All rights reserved.

// メートル単位で出力
public final class CoordsUtil {
    // Copyright © 2007-2012 やまだらけ http://yamadarake.jp/trdi/report000001.html
    public static final double BESSEL_A = 6377397.155;
    public static final double BESSEL_E2 = 0.00667436061028297;
    public static final double BESSEL_MNUM = 6334832.10663254;

    public static final double GRS80_A = 6378137.000;
    public static final double GRS80_E2 = 0.00669438002301188;
    public static final double GRS80_MNUM = 6335439.32708317;

    public static final double WGS84_A = 6378137.000;
    public static final double WGS84_E2 = 0.00669437999019758;
    public static final double WGS84_MNUM = 6335439.32729246;

    public static final int BESSEL = 0;
    public static final int GRS80 = 1;
    public static final int WGS84 = 2;

    public static final double calcDistHubeny(double lat1, double lng1, double lat2, double lng2) {
        return calcDistHubeny(lat1, lng1, lat2, lng2, WGS84_A, WGS84_E2, WGS84_MNUM);
    }

    public static final double calcDistHubeny(double lat1, double lng1, double lat2, double lng2, double a, double e2, double mnum) {
        final double my = deg2rad(( lat1 + lat2 ) / 2.0);
        final double dy = deg2rad(lat1 - lat2);
        final double dx = deg2rad(lng1 - lng2);

        final double sin = Math.sin(my);
        final double w = Math.sqrt(1.0 - ( e2 * sin * sin ));
        final double m = mnum / ( w * w * w );
        final double n = a / w;

        final double dym = dy * m;
        final double dxncos = dx * n * Math.cos(my);

        return Math.sqrt(( dym * dym ) + ( dxncos * dxncos ));
    }

    public static final double calcDistHubery(double lat1, double lng1, double lat2, double lng2, int type) {
        switch (type) {
            case BESSEL:
                return calcDistHubeny(lat1, lng1, lat2, lng2, BESSEL_A, BESSEL_E2, BESSEL_MNUM);
            case GRS80:
                return calcDistHubeny(lat1, lng1, lat2, lng2, GRS80_A, GRS80_E2, GRS80_MNUM);
            default:
                return calcDistHubeny(lat1, lng1, lat2, lng2, WGS84_A, WGS84_E2, WGS84_MNUM);
        }
    }

    public static final double deg2rad(double deg) {
        return ( deg * Math.PI ) / 180.0;
    }

// System.out.println("Distance = " + calcDistHubeny(lat1, lng1, lat2, lng2) + " m");
}
